package com.honghe.party.common;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.*;

public class JsonStringTypeHandler extends BaseTypeHandler<String> {
    public void setNonNullParameter(PreparedStatement statement, int index, String value, JdbcType jdbcType) throws SQLException {
        statement.setObject(index, value, Types.OTHER);
    }
    public String getNullableResult(ResultSet result, String column) throws SQLException { return result.getString(column); }
    public String getNullableResult(ResultSet result, int column) throws SQLException { return result.getString(column); }
    public String getNullableResult(CallableStatement statement, int column) throws SQLException { return statement.getString(column); }
}
