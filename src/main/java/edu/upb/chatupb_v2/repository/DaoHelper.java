package edu.upb.chatupb_v2.repository;

import lombok.extern.slf4j.Slf4j;

import java.net.ConnectException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Autor      :Ricardo Laredo
 * Date       :21-11-18
 */
@Slf4j
public class DaoHelper<T>  {

    public DaoHelper() {
        super();
    }

    public interface QueryParameters {
        void setParameters(PreparedStatement pst) throws SQLException;
    }

    public interface ResultReader<T> {
        T getResult(ResultSet result) throws SQLException;
    }

    public interface ResultProcedureReader<T> {
        T getResult(CallableStatement callableStatement) throws SQLException;
    }

    public List<T> executeQuery(String query, ResultReader<T> reader) throws ConnectException, SQLException {
        return executeQuery(query, null, reader);
    }

    public List<T> executeQuery(String query, QueryParameters params, ResultReader<T> reader)
            throws ConnectException, SQLException {

        Connection conn;
        PreparedStatement st = null;

        try {
            conn = ConnectionDB.getInstance().getConection();
        } catch (Exception ex) {
            log.error("No se logro crear conexion a la base de datos", ex);
            throw new ConnectException("No se logro crear conexion a la base de datos");
        }

        try {
            st = conn.prepareStatement(query);

            if (params != null) {
                params.setParameters(st);
            }

            boolean status = st.execute();
            if (!status) {
                log.info("Estado de Ejecucion: {}", status);
            }

            if (status) {
                List<T> results = new ArrayList<>();
                try (ResultSet result = st.getResultSet()) {
                    while (result.next()) {
                        T value = reader.getResult(result);
                        if (value != null) {
                            results.add(value);
                        }
                    }
                }
                return results;
            }

            return new ArrayList<>();

        } catch (SQLException e) {
            log.error("Excepcion sql al ejecutar la query : {}  causa => {}", query, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error desconocido al ejecutar query : {}", query, e);
            throw e;
        } finally {
            if (st != null) st.close();
            // conn siempre debería existir si llegamos aquí, pero lo mantenemos seguro
            try {
                if (conn != null && !conn.isClosed()) conn.close();
            } catch (SQLException ignored) {}
        }
    }

    protected void insert(String query, QueryParameters params, Model model) throws Exception {
        Connection conn = null;

        try {
            conn = ConnectionDB.getInstance().getConection();
        } catch (Exception ex) {
            log.info("No se logro crear conexion a la base de datos", ex);
            throw new SQLException(ex);
        }

        try (PreparedStatement st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            if (params != null) {
                params.setParameters(st);
            }

            if (st.executeUpdate() > 0) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        model.setId(rs.getLong(1));
                    }
                }
            }

        } catch (SQLException e) {
            log.error("Excepcion sql al ejecutar la query : {}  causa => {}", query, e.getMessage());
            throw new SQLException(e);
        } catch (Exception e) {
            log.error("Error desconocido al ejecutar query : {}", query, e);
            throw new Exception(e);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) conn.close();
            } catch (SQLException ignored) {}
        }
    }

    /**
     * Metodo actualizar
     */
    protected void update(String query, QueryParameters params) throws ConnectException, SQLException {
        Connection conn;
        PreparedStatement st = null;

        try {
            conn = ConnectionDB.getInstance().getConection();
        } catch (Exception ex) {
            log.error("No se logro crear conexion a la base de datos", ex);
            throw new ConnectException("No se logro crear conexion a la base de datos");
        }

        try {
            st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            if (params != null) {
                params.setParameters(st);
            }

            st.executeUpdate();

        } catch (SQLException e) {
            log.error("Excepcion sql al ejecutar la query : {}  causa => {}", query, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error desconocido al ejecutar query : {}", query, e);
            throw e;
        } finally {
            if (st != null) st.close();
            try {
                if (conn != null && !conn.isClosed()) conn.close();
            } catch (SQLException ignored) {}
        }
    }

    int executeQueryCount(String query, QueryParameters params)
            throws ConnectException, SQLException {

        Connection conn;

        try {
            conn = ConnectionDB.getInstance().getConection();
        } catch (Exception ex) {
            log.error("No se logro crear conexion a la base de datos", ex);
            throw new ConnectException("No se logro crear conexion a la base de datos");
        }

        try (PreparedStatement st = conn.prepareStatement(query)) {

            if (params != null) {
                params.setParameters(st);
            }

            boolean status = st.execute();
            if (status) {
                int cantRows = -1;
                try (ResultSet result = st.getResultSet()) {
                    if (result.next()) {
                        cantRows = result.getInt(1);
                    }
                }
                return cantRows;
            }

            return -1;

        } catch (SQLException e) {
            log.error("Excepcion sql al ejecutar la query : {}  causa => {}", query, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error desconocido al ejecutar query : {}", query, e);
            throw e;
        } finally {
            try {
                if (conn != null && !conn.isClosed()) conn.close();
            } catch (SQLException ignored) {}
        }
    }

    /**
     * Metodo para llamar a un procedimiento almacenado
     */
    protected T executeProcedureStore(String query, QueryParameters params, ResultProcedureReader<T> reader) throws Exception {

        Connection conn;
        CallableStatement st = null;

        try {
            conn = ConnectionDB.getInstance().getConection();
        } catch (Exception ex) {
            log.info("No se logro crear conexion a la base de datos", ex);
            throw new SQLException(ex);
        }

        try {
            st = conn.prepareCall(query);

            if (params != null) {
                params.setParameters(st);
            }

            T value = null;
            if (st.execute()) {
                value = reader.getResult(st);
            }
            return value;

        } catch (SQLException e) {
            log.error("Excepcion sql al ejecutar la query : {}  causa => {}", query, e.getMessage());
            throw new SQLException(e);
        } catch (Exception e) {
            log.error("Error desconocido al ejecutar query : {}", query, e);
            throw new Exception(e);
        } finally {
            if (st != null) st.close();
            try {
                if (conn != null && !conn.isClosed()) conn.close();
            } catch (SQLException ignored) {}
        }
    }
}