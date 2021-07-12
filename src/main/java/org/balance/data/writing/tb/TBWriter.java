package org.balance.data.writing.tb;

import com.formdev.flatlaf.extras.components.FlatProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.balance.data.objects.Balances;
import org.balance.data.utils.DataUtils;
import org.balance.data.writing.Writer;
import org.balance.extractor.processes.Extractor.Task;
import org.jdesktop.swingx.JXLabel;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STTotalsRowFunction;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Nicholas Curl
 */
public class TBWriter extends Writer {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(TBWriter.class);


    private final Balances balances;
    private final Task<?>  task;

    public TBWriter(Path workbookFile, Balances balances, Task<?> task) {
        this(workbookFile.toFile(), balances, task);
    }

    public TBWriter(File workbookFile, Balances balances, Task<?> task) {
        super(workbookFile);
        this.balances = balances;
        this.task = task;
    }

    @Override
    public void makeWorkbook() {
        FlatProgressBar progressBar = task.getProgressContainer().getProgressBar();
        JXLabel status = task.getProgressContainer().getStatus();
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Balances");
        XSSFRow headerRow = sheet.createRow(0);
        List<Object> header = balances.getHeader();
        List<String> depts = balances.getDepts();
        List<String> accountNums = balances.getAccountNums();
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
        XSSFCellStyle moneyStyle = workbook.createCellStyle();
        XSSFDataFormat format = workbook.getCreationHelper().createDataFormat();
        short formatIndex = format.getFormat("\"$\"#,##0.00");
        moneyStyle.setDataFormat(formatIndex);
        for (int i = 0; i < header.size(); i++) {
            if (task.isCancelled()) {
                return;
            }
            Object object = header.get(i);
            XSSFCell cell = headerRow.createCell(i);
            DataUtils.setCellValue(cell, object);
            cell.setCellStyle(bold);
        }
        int rowNum = 1;
        int cellNum = 2;
        int progress = 0;
        XSSFFormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        progressBar.setMaximum(accountNums.size() * depts.size());
        status.setText("Writing");
        for (String accountNum : accountNums) {
            if (task.isCancelled()) {
                return;
            }
            XSSFRow row = sheet.createRow(rowNum);
            Map<String, BigDecimal> debitRow = balances.getDebit().get(accountNum);
            Map<String, BigDecimal> creditRow = balances.getCredit().get(accountNum);
            XSSFCell col1 = row.createCell(0);
            col1.setCellValue(accountNum);
            XSSFCell col2 = row.createCell(1);
            col2.setBlank();
            for (String dept : depts) {
                if (task.isCancelled()) {
                    return;
                }
                XSSFCell cell = row.createCell(cellNum);
                BigDecimal debitBigDecimal = debitRow.get(dept);
                BigDecimal creditBigDecimal = creditRow.get(dept);
                BigDecimal amountBigDecimal = debitBigDecimal.add(creditBigDecimal);
                DataUtils.setCellValue(cell, amountBigDecimal.doubleValue());
                cell.setCellStyle(moneyStyle);
                cellNum++;
                progressBar.setValue(progress++);
            }
            cellNum = 2;
            rowNum++;
        }
        CellReference startingReference = new CellReference(sheet.getRow(0)
                                                                 .getCell(sheet.getRow(0).getFirstCellNum()));
        CellReference endReference = new CellReference(sheet.getRow(sheet.getLastRowNum())
                                                            .getCell(sheet.getRow(sheet.getLastRowNum())
                                                                          .getLastCellNum() - 1));
        AreaReference reference = new AreaReference(startingReference, endReference, SpreadsheetVersion.EXCEL2007);
        XSSFTable table = sheet.createTable(reference);
        table.getCTTable().addNewTableStyleInfo();
        XSSFTableStyleInfo style = (XSSFTableStyleInfo) table.getStyle();
        style.setName("TableStyleMedium2");
        style.setShowColumnStripes(false);
        style.setShowRowStripes(true);
        style.setFirstColumn(false);
        style.setLastColumn(false);
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            XSSFRow row = sheet.getRow(i);
            XSSFCell cell = row.getCell(1);
            cell.setCellFormula("SUM(" +
                                table.getName() +
                                "[@[" +
                                table.getColumns().get(2).getName() +
                                "]:[" +
                                table.getColumns().get(table.getColumns().size() - 1).getName() +
                                "]])");
            evaluator.evaluateFormulaCell(cell);
            cell.setCellStyle(moneyStyle);
        }
        table.getCTTable().setTotalsRowCount(1);
        table.getCTTable()
             .getTableColumns()
             .getTableColumnList()
             .get(0)
             .setTotalsRowLabel("Total");
        table.getCTTable()
             .getTableColumns()
             .getTableColumnList()
             .get(1)
             .setTotalsRowFunction(STTotalsRowFunction.SUM);
        CTAutoFilter autoFilter = table.getCTTable().addNewAutoFilter();
        autoFilter.setRef(reference.formatAsString());
        String formula = "SUBTOTAL(109," + table.getName() + "[" + table.getColumns().get(1).getName() + "])";
        XSSFRow xssfRow = sheet.createRow(table.getEndRowIndex() + 1);
        for (int i = 0; i < DataUtils.getLastColumn(sheet); i++) {
            xssfRow.createCell(i).setBlank();
        }
        endReference = new CellReference(sheet.getRow(sheet.getLastRowNum())
                                              .getCell(sheet.getRow(sheet.getLastRowNum())
                                                            .getLastCellNum() - 1));
        reference = new AreaReference(startingReference, endReference, SpreadsheetVersion.EXCEL2007);
        table.setCellReferences(reference);
        sheet.getRow(sheet.getLastRowNum()).getCell(0).setCellValue("Total");
        XSSFCell xssfCell = sheet.getRow(sheet.getLastRowNum()).getCell(1);
        xssfCell.setCellFormula(formula);
        evaluator.evaluate(xssfCell);
        xssfCell.setCellStyle(moneyStyle);
        if (xssfCell.getNumericCellValue() != 0) {
            logger.warn("Total does not balance to 0 for workbook {}", getWorkbookFile().toString());
        }
        for (int i = 0; i < DataUtils.getLastColumn(sheet); i++) {
            if (task.isCancelled()) {
                return;
            }
            sheet.autoSizeColumn(i);
        }
        if (!task.isCancelled()) {
            status.setText("Saving");
            progressBar.setValue(0);
            progressBar.setStringPainted(false);
            progressBar.setIndeterminate(true);
            try {
                workbook.write(getFileOutputStream());
                workbook.close();
                getFileOutputStream().close();
            }
            catch (IOException e) {
                logger.fatal("Unable to write file", e);
                System.exit(1);
            }
            progressBar.setIndeterminate(false);
            progressBar.setStringPainted(true);
        }
        else {
            try {
                workbook.close();
            }
            catch (IOException e) {
                logger.fatal(e.getMessage(), e);
            }
        }
    }
}
