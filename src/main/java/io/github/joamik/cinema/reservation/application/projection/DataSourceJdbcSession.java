package io.github.joamik.cinema.reservation.application.projection;

import akka.japi.function.Function;
import akka.projection.jdbc.JdbcSession;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceJdbcSession implements JdbcSession {

    private final Connection connection;

    public DataSourceJdbcSession(DataSource dataSource) {
        try {
            this.connection = dataSource.getConnection();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <Result> Result withConnection(Function<Connection, Result> func) throws Exception {
        return func.apply(connection);
    }

    @Override
    public void commit() throws SQLException {
        connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        connection.rollback();
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
