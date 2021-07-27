package org.balance.data.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.balance.data.objects.Data;
import org.balance.utils.Utils;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;

import java.io.*;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Nicholas Curl
 */
public class DataUtils {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(DataUtils.class);

    public static List<Object> getHeader(Path workbookFile) throws IOException, InvalidFormatException {
        return getHeader(workbookFile.toFile());
    }

    public synchronized static List<Object> getHeader(InputStream stream) throws IOException, InvalidFormatException {
        OPCPackage pkg = OPCPackage.open(stream);
        return getHeader(pkg);
    }

    private synchronized static List<Object> getHeader(OPCPackage pkg) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(pkg);
        XSSFFormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        XSSFSheet sheet = workbook.getSheetAt(1);
        XSSFRow firstRow = sheet.getRow(sheet.getFirstRowNum());
        List<Object> header = new ArrayList<>();
        for (int i = 0; i < firstRow.getLastCellNum(); i++) {
            XSSFCell cell = firstRow.getCell(i);
            header.add(getCellValue(cell, evaluator));
        }
        pkg.revert();
        return Utils.trimList(header);
    }

    public synchronized static List<Object> getHeader(File workbookFile) throws IOException, InvalidFormatException {
        OPCPackage pkg = OPCPackage.open(workbookFile);
        return getHeader(pkg);
    }

    private static Object getCellValue(XSSFCell cell, XSSFFormulaEvaluator evaluator) {
        switch (cell.getCellType()) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                else {
                    return cell.getNumericCellValue();
                }
            case FORMULA:
                evaluator.evaluate(cell);
                switch (cell.getCachedFormulaResultType()) {
                    case STRING:
                        return cell.getStringCellValue().trim();
                    case BOOLEAN:
                        return cell.getBooleanCellValue();
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            return cell.getDateCellValue();
                        }
                        else {
                            return cell.getNumericCellValue();
                        }
                    default:
                        return null;
                }
            default:
                return null;
        }
    }

    public static int getLastColumn(XSSFSheet sheet) {
        int lastColumn = 0;
        for (Row row : sheet) {
            if ((int) row.getLastCellNum() > lastColumn) {
                lastColumn = row.getLastCellNum();
            }
        }
        return lastColumn;
    }

    public synchronized static InputStream getResource(Class<?> clazz, String resource) throws URISyntaxException {
        return Objects.requireNonNull(clazz.getClassLoader().getResourceAsStream(resource));
    }

    public static void makeEditable(Path workbookFile) {
        try {
            Path tempFile = Paths.get(workbookFile.toString().replace("xlsx", "tmp"));
            XSSFWorkbook workbook = new XSSFWorkbook(workbookFile.toFile());
            FileOutputStream outputStream = new FileOutputStream(tempFile.toFile());
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
            Files.move(tempFile, workbookFile, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException | InvalidFormatException e) {
            logger.fatal("Unable to make workbook editable", e);
            System.exit(1);
        }
    }

    public static Data readData(File workbookFile) throws InvalidFormatException, IOException {
        List<List<Object>> data = new ArrayList<>();
        OPCPackage opcPackage = OPCPackage.open(workbookFile);
        XSSFWorkbook workbook = new XSSFWorkbook(opcPackage);
        XSSFFormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        XSSFSheet sheet = workbook.getSheetAt(0);
        XSSFTable table = sheet.getTables().get(0);
        List<Object> header = new ArrayList<>();
        for (int i = table.getStartColIndex(); i < table.getEndColIndex() + 1; i++) {
            header.add(getCellValue(sheet.getRow(table.getStartRowIndex()).getCell(i), formulaEvaluator));
        }
        for (int i = table.getStartRowIndex() + 1; i < (table.getRowCount() - table.getTotalsRowCount()); i++) {
            List<Object> row = new ArrayList<>();
            for (int j = table.getStartColIndex(); j < table.getEndColIndex() + 1; j++) {
                XSSFCell cell = sheet.getRow(i).getCell(j);
                row.add(getCellValue(cell, formulaEvaluator));
            }
            data.add(row);
        }
        opcPackage.revert();
        return new Data(header, data);
    }

    public static void saveChanges(OPCPackage pkg, XSSFWorkbook workbook) {
        try {
            OutputStream out = OutputStream.nullOutputStream();
            workbook.write(out);
            pkg.close();
            out.close();
        }
        catch (IOException e) {
            logger.fatal("Unable to save changes", e);
            System.exit(1);
        }
    }

    public static void writeWorkbook(List<List<Object>> table, File workbookFile)
    throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Trial Balance");
        XSSFCellStyle bold = workbook.createCellStyle();
        XSSFFont font = workbook.findFont(true,
                                          IndexedColors.AUTOMATIC.getIndex(),
                                          XSSFFont.DEFAULT_FONT_SIZE,
                                          XSSFFont.DEFAULT_FONT_NAME,
                                          false,
                                          false,
                                          XSSFFont.SS_NONE,
                                          XSSFFont.U_NONE
        );
        bold.setFont(font);
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFDataFormat format = workbook.getCreationHelper().createDataFormat();
        short formatIndex = format.getFormat("\"$\"#,##0.00");
        style.setDataFormat(formatIndex);
        for (int i = 0; i < table.size(); i++) {
            List<Object> objects = table.get(i);
            XSSFRow row = sheet.createRow(i);
            for (int j = 0; j < objects.size(); j++) {
                Object object = objects.get(j);
                XSSFCell cell = row.createCell(j);
                setCellValue(cell,object);
                if(object instanceof Double || object instanceof BigDecimal){
                    cell.setCellStyle(style);
                }
                else if (i == 0 && object instanceof String) {
                    cell.setCellStyle(bold);
                }
            }
        }
        CellReference startReference = new CellReference(0, 0);
        CellReference endReference = new CellReference(sheet.getLastRowNum(),
                                                       sheet.getRow(sheet.getLastRowNum()).getLastCellNum()-1
        );
        AreaReference reference = new AreaReference(startReference, endReference, SpreadsheetVersion.EXCEL2007);
        XSSFTable xssfTable = sheet.createTable(reference);
        xssfTable.getCTTable().addNewTableStyleInfo();
        XSSFTableStyleInfo tableStyle = (XSSFTableStyleInfo) xssfTable.getStyle();
        tableStyle.setName("TableStyleMedium2");
        tableStyle.setShowColumnStripes(false);
        tableStyle.setShowRowStripes(true);
        tableStyle.setFirstColumn(false);
        tableStyle.setLastColumn(false);
        CTAutoFilter autoFilter = xssfTable.getCTTable().addNewAutoFilter();
        autoFilter.setRef(reference.formatAsString());
        for (int i = 0; i < xssfTable.getEndColIndex() + 1; i++) {
            sheet.autoSizeColumn(i);
        }
        FileOutputStream outputStream = new FileOutputStream(workbookFile);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    public static void setCellValue(XSSFCell cell, Object object){
        if (object instanceof Integer) {
            int integer = (int) object;
            cell.setCellValue(integer);
        }
        else if (object instanceof Double) {
            double aDouble = (double) object;
            cell.setCellValue(aDouble);
        }
        else if (object instanceof Boolean) {
            boolean aBoolean = (boolean) object;
            cell.setCellValue(aBoolean);
        }
        else if (object instanceof Date) {
            Date date = (Date) object;
            cell.setCellValue(date);
        }
        else if (object instanceof Calendar) {
            Calendar calendar = (Calendar) object;
            cell.setCellValue(calendar);
        }
        else if (object instanceof LocalDate) {
            LocalDate localDate = (LocalDate) object;
            cell.setCellValue(localDate);
        }
        else if (object instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) object;
            cell.setCellValue(localDateTime);
        }
        else if (object instanceof RichTextString) {
            RichTextString richTextString = (RichTextString) object;
            cell.setCellValue(richTextString);
        }
        else if (object instanceof String) {
            String s = (String) object;
            cell.setCellValue(s);
        }
        else if(object instanceof BigDecimal){
            BigDecimal bigDecimal = (BigDecimal) object;
            cell.setCellValue(bigDecimal.doubleValue());
        }
        else {
            cell.setBlank();
        }
    }
}
