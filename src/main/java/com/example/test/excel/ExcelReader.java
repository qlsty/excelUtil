package com.example.test.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public final class ExcelReader {

    private static final Map<Class<?>, Map<Field, FieldPaser>> fieldPaserCache = new ConcurrentHashMap<>();

    public static class ExcelContext {
        private File file;
        private Sheet sheet;
        private Workbook workbook;
        private boolean skipHeader;
        private int headerLines;

        public ExcelContext forSheet(int num) {
            this.sheet = workbook.getSheetAt(num);
            return this;
        }

        public ExcelContext forSheet(String name) {
            this.sheet = workbook.getSheet(name);
            return this;
        }

        public ExcelContext skipHeader() {
            this.skipHeader = true;
            return this;
        }

        public ExcelContext setHeaderLines(int headerLines) {
            this.headerLines = headerLines;
            return this;
        }

        public <T> List<T> parseObject(Class<T> clazz) {
            ArrayList<T> objects = new ArrayList<>();
            fillValue(clazz, objects);
            return objects;
        }

        public List<String> asCSV() {
            ArrayList<String> list = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            this.consumerRows((row, index) -> {
                int cells = row.getPhysicalNumberOfCells();
                for (int i = 0; i < cells; i++) {
                    builder.append(getCellValue(row.getCell(i)));
                    builder.append(",");
                }
                String string = builder.toString();
                list.add(string.substring(0, string.length() - 1));
                builder.delete(0, string.length());
            });
            return list;
        }

        public String asJson() {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            Row header = this.sheet.getRow(0);
            int headerCells = header.getPhysicalNumberOfCells();
            int rows = this.sheet.getPhysicalNumberOfRows();

            this.consumerRows((row, index) -> {
                if (index != 0) {
                    builder.append("{");
                    for (int i = 0; i < headerCells; i++) {
                        builder.append("\"");
                        builder.append(getCellValue(header.getCell(i)));
                        builder.append("\"");
                        builder.append(":");
                        builder.append("\"");
                        builder.append(getCellValue(row.getCell(i)));
                        builder.append("\"");
                        if (i != headerCells - 1) {
                            builder.append(",");
                        }
                    }
                    builder.append("}");
                    if (index != rows - 1) {
                        builder.append(",");
                    }
                }
            });

            builder.append("]");
            return builder.toString();
        }

        public void consumerRows(BiConsumer<Row, Integer> rowConsumer) {
            int rows = this.sheet.getPhysicalNumberOfRows();
            int start = skipHeader ? headerLines : 0;
            for (int i = start; i < rows; i++) {
                rowConsumer.accept(this.sheet.getRow(i), i);
            }
        }


        private <T> void fillValue(Class<T> clazz, ArrayList<T> objects) {

            Map<Field, FieldPaser> fieldFieldPaserMap = getFieldFieldPaserMap(clazz);

            Map<Field, Integer> fieldIndexMap = locate(clazz);
            ExcelPaser annotation = clazz.getDeclaredAnnotation(ExcelPaser.class);
            boolean allNotNull = annotation != null && annotation.required();

            this.consumerRows((row, index) -> {
                try {
                    T target = clazz.newInstance();
                    for (Field field : fieldFieldPaserMap.keySet()) {
                        field.setAccessible(true);

                        ExcelPaser excelPaserAnno = field.getDeclaredAnnotation(ExcelPaser.class);
                        boolean fieldNotNull = excelPaserAnno != null && excelPaserAnno.required();
                        fieldNotNull = fieldNotNull || allNotNull;

                        String cellValue = getCellValue(row.getCell(fieldIndexMap.get(field)));

                        if (fieldNotNull && cellValue == null) {
                            throw new RuntimeException(index + " col " + fieldIndexMap.get(field) + " row " + "can not blank");
                        }

                        Object var = fieldFieldPaserMap.get(field).paser(cellValue, field.getType());
                        field.set(target, var);
                    }

                    objects.add(target);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        }

        private <T> Map<Field, Integer> locate(Class<T> clazz) {
            Map<Field, Integer> map = new HashMap<>();
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                map.putIfAbsent(fields[i], i);
            }
            return map;
        }


        private <T> Map<Field, FieldPaser> getFieldFieldPaserMap(Class<T> clazz) {

            Map<Field, FieldPaser> fieldFieldPaserMap = fieldPaserCache.get(clazz);

            if (fieldFieldPaserMap == null) {
                fieldFieldPaserMap = buildFieldMap(clazz);
                fieldPaserCache.putIfAbsent(clazz, fieldFieldPaserMap);
            }
            return fieldFieldPaserMap;
        }

        private <T> Map<Field, FieldPaser> buildFieldMap(Class<T> clazz) {

            HashMap<Field, FieldPaser> map = new HashMap<>();

            for (Field declaredField : clazz.getDeclaredFields()) {
                ExcelPaser annotation = declaredField.getDeclaredAnnotation(ExcelPaser.class);

                if (annotation == null) {
                    map.put(declaredField, FieldPaser.FieldPaser0);
                    continue;
                }

                if (annotation.ignore()) {
                    continue;
                }

                try {
                    map.put(declaredField, annotation.paserClass().newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            return map;
        }


        public ExcelContext(File file) throws IOException {
            this.file = file;
            if (isOldVersion(file)) {
                workbook = new HSSFWorkbook(new FileInputStream(file));
            } else {
                workbook = new XSSFWorkbook(new FileInputStream(file));
            }
            this.forSheet(0);
        }
    }


    public static ExcelContext read(File file) {
        try {
            return new ExcelContext(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isOldVersion(File file) {
        return file.getName().endsWith("xls");
    }


    public static String getCellValue(Cell cell) {
        String cellValue = null;
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    cellValue = sdf.format(org.apache.poi.ss.usermodel.DateUtil.getJavaDate(cell.getNumericCellValue()));
                } else {
                    DataFormatter dataFormatter = new DataFormatter();
                    cellValue = dataFormatter.formatCellValue(cell);
                }
                break;
            case Cell.CELL_TYPE_STRING: // 字符串
                cellValue = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_BOOLEAN: // Boolean
                cellValue = cell.getBooleanCellValue() + "";
                break;
            case Cell.CELL_TYPE_FORMULA: // 公式
                cellValue = cell.getCellFormula() + "";
                break;
            case Cell.CELL_TYPE_BLANK: // 空值
                break;
            case Cell.CELL_TYPE_ERROR: // 故障
                break;
            default:
                break;
        }
        return cellValue;
    }


    public static void main(String[] args) {
        File file = new File("C:\\Users\\Administrator\\Downloads\\货品档案-导入模板.xls");
        List<TestBean> into = ExcelReader.read(file).skipHeader().setHeaderLines(1).parseObject(TestBean.class);
        System.out.println(into);
        String json = ExcelReader.read(file).skipHeader().setHeaderLines(1).asJson();
        System.out.println(json);
        List<String> strings = ExcelReader.read(file).skipHeader().setHeaderLines(1).asCSV();
        System.out.println(strings);

    }

}
