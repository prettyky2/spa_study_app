package com.example.myapp;

public class AppExcelRow {
    private String column1; // 엑셀의 1열 데이터
    private String column2; // 엑셀의 2열 데이터
    private String column3; // 엑셀의 3열 데이터
    private String column4; // 엑셀의 4열 데이터

    public AppExcelRow(String column1, String column2, String column3, String column4) {
        this.column1 = column1;
        this.column2 = column2;
        this.column3 = column3;
        this.column4 = column4;
    }

    // Getter 메서드 추가
    public String getColumn1() {
        return column1;
    }

    public String getColumn2() {
        return column2;
    }

    public String getColumn3() {
        return column3;
    }

    public String getColumn4() {
        return column4;
    }
}