package com.gateway.limoapi;

import com.gateway.limoapi.model.RepMoveModel;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReplaceMapper implements RowMapper<RepMoveModel> {

    @Override
    public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new RepMoveModel();
    }
}
