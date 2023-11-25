package com.jeremy.pandathon;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    public Connection connectionSQL() throws SQLException {
        String url = "jdbc:postgresql://ptpostgresql.postgres.database.azure.com:5432/PandaSQL";
        String user = "Panda";
        String password = "Tech1234";
        Connection connection = DriverManager.getConnection(url, user, password);
        return connection;
    }

    @FunctionName("GetClima")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws SQLException {

        context.getLogger().info("Java HTTP trigger processed a request.");

        Connection dbConnection = null;
        ResultSet result = null;
        String jsonReturn = "";

        try {
            dbConnection = connectionSQL();

            final String query = "SELECT idclima, clima, pais, ciudad, nombre, cedula, registro FROM weather";
            PreparedStatement prepare = dbConnection.prepareStatement(query);

            result = prepare.executeQuery();
        } catch(SQLException e){
            e.printStackTrace();
        } finally {
            dbConnection.close();
        }

        if(result!=null){
            jsonReturn += "[";
            while (result.next())
            {
                jsonReturn += "{\"idclima\":"+result.getInt("idclima")+",";
                jsonReturn += "\"clima\":\""+result.getString("clima")+"\",";
                jsonReturn += "\"pais\":\""+result.getString("pais")+"\",";
                jsonReturn += "\"ciudad\":\""+result.getString("ciudad")+"\",";
                jsonReturn += "\"nombre\":\""+result.getString("nombre")+"\",";
                jsonReturn += "\"cedula\":\""+result.getString("cedula")+"\",";
                jsonReturn += "\"registro\":\""+result.getDate("registro")+"\"}";
            }
            jsonReturn += "]";
        }

        if (jsonReturn.equals("")) 
        {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("The database is empty or something went wrong.").build();
        } else 
        {
            return request.createResponseBuilder(HttpStatus.OK).body(jsonReturn).build();
        }
    }
}