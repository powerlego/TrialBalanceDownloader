package org.balance.data.writing.template;

import com.formdev.flatlaf.extras.components.FlatProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
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

/**
 * @author Nicholas Curl
 */
public class TemplateWriter extends Writer {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(TemplateWriter.class);

    private final Task<?> task;
    private final List<List<Object>> mapped;

    public TemplateWriter(Task<?> task, Path workbookFile, List<List<Object>> mapped) {
        this(task,workbookFile.toFile(),mapped);
    }

    public TemplateWriter(Task<?> task, File workbookFile, List<List<Object>> mapped) {
        super(workbookFile);
        this.task = task;
        this.mapped = mapped;
    }

    @Override
    public void makeWorkbook() {
        FlatProgressBar progressBar = task.getProgressContainer().getProgressBar();
        JXLabel status = task.getProgressContainer().getStatus();
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Data");
        XSSFRow headerRow = sheet.createRow(0);
        List<Object> header = mapped.get(0);
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
        XSSFCellStyle money = workbook.createCellStyle();
        XSSFDataFormat format = workbook.getCreationHelper().createDataFormat();
        short formatIndex = format.getFormat("\"$\"#,##0.00");
        money.setDataFormat(formatIndex);
        progressBar.setMaximum(mapped.size() * header.size());
        int progress = 0;
        status.setText("Writing");
        for (int i = 0; i < header.size(); i++) {
            if(task.isCancelled()){
                return;
            }
            Object object = header.get(i);
            XSSFCell cell = headerRow.createCell(i);
            DataUtils.setCellValue(cell, object);
            cell.setCellStyle(bold);
        }
        for(int i = 1; i<mapped.size(); i++){
            if(task.isCancelled()){
                return;
            }
            List<Object> mappedRow = mapped.get(i);
            if(mappedRow.isEmpty()){
                continue;
            }
            XSSFRow row = sheet.createRow(i);
            for (int j = 0; j<header.size(); j++){
                if(task.isCancelled()){
                    return;
                }
                XSSFCell cell = row.createCell(j);
                DataUtils.setCellValue(cell, mappedRow.get(j));
                if (mappedRow.get(j) instanceof Double || mappedRow.get(j) instanceof BigDecimal) {
                    cell.setCellStyle(money);
                }
                progressBar.setValue(progress++);
            }
        }
        CellReference startReference = new CellReference(0, 0);
        CellReference endReference = new CellReference(sheet.getLastRowNum(),
                                                       sheet.getRow(sheet.getLastRowNum()).getLastCellNum() - 1
        );
        AreaReference reference = new AreaReference(startReference, endReference, SpreadsheetVersion.EXCEL2007);
        XSSFTable table = sheet.createTable(reference);
        table.getCTTable().addNewTableStyleInfo();
        XSSFTableStyleInfo tableStyle = (XSSFTableStyleInfo) table.getStyle();
        tableStyle.setName("TableStyleMedium2");
        tableStyle.setShowColumnStripes(false);
        tableStyle.setShowRowStripes(true);
        tableStyle.setFirstColumn(false);
        tableStyle.setLastColumn(false);
        table.getCTTable().setTotalsRowCount(1);
        table.getCTTable()
             .getTableColumns()
             .getTableColumnList()
             .get(0)
             .setTotalsRowLabel("Total");
        table.getCTTable()
             .getTableColumns()
             .getTableColumnList()
             .get(7)
             .setTotalsRowFunction(STTotalsRowFunction.SUM);
        table.getCTTable()
             .getTableColumns()
             .getTableColumnList()
             .get(8)
             .setTotalsRowFunction(STTotalsRowFunction.SUM);
        CTAutoFilter autoFilter = table.getCTTable().addNewAutoFilter();
        autoFilter.setRef(reference.formatAsString());
        String formula1 = "SUBTOTAL(109," + table.getName() + "[" + table.getColumns().get(7).getName() + "])";
        String formula2 = "SUBTOTAL(109," + table.getName() + "[" + table.getColumns().get(8).getName() + "])";
        XSSFRow xssfRow = sheet.createRow(table.getEndRowIndex() + 1);
        for (int i = 0; i < DataUtils.getLastColumn(sheet); i++) {
            if(task.isCancelled()){
                return;
            }
            xssfRow.createCell(i).setBlank();
        }
        endReference = new CellReference(sheet.getRow(sheet.getLastRowNum())
                                              .getCell(sheet.getRow(sheet.getLastRowNum())
                                                            .getLastCellNum() - 1));
        reference = new AreaReference(startReference, endReference, SpreadsheetVersion.EXCEL2007);
        table.setCellReferences(reference);
        XSSFFormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        sheet.getRow(sheet.getLastRowNum()).getCell(0).setCellValue("Total");
        XSSFCell xssfCell1 = sheet.getRow(sheet.getLastRowNum()).getCell(7);
        xssfCell1.setCellFormula(formula1);
        evaluator.evaluate(xssfCell1);
        xssfCell1.setCellStyle(money);
        XSSFCell xssfCell2 = sheet.getRow(sheet.getLastRowNum()).getCell(8);
        xssfCell2.setCellFormula(formula2);
        evaluator.evaluate(xssfCell2);
        xssfCell2.setCellStyle(money);
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
        progressBar.setValue(0);
        status.setText("");
    }
}
