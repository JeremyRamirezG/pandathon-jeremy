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

public class FunctionPost {
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

    @FunctionName("PostClima")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws SQLException {

        context.getLogger().info("Java HTTP trigger processed a request.");

        Connection dbConnection = null;
        ResultSet result = null;

        final String querycl = request.getQueryParameters().get("clima");
        final String clima = request.getBody().orElse(querycl);
        final String queryp = request.getQueryParameters().get("pais");
        final String pais = request.getBody().orElse(queryp);
        final String queryci = request.getQueryParameters().get("ciudad");
        final String ciudad = request.getBody().orElse(queryci);
        final String queryn = request.getQueryParameters().get("nombre");
        final String nombre = request.getBody().orElse(queryn);
        final String queryce = request.getQueryParameters().get("cedula");
        final String cedula = request.getBody().orElse(queryce);

        try {
            dbConnection = connectionSQL();

            final String query = "INSERT INTO weather ('clima','pais','ciudad','nombre','cedula') VALUES ('"+clima+"','"+pais+"','"+ciudad+"','"+nombre+"','"+cedula+"')";
            PreparedStatement prepare = dbConnection.prepareStatement(query);

            result = prepare.executeQuery();
        } catch(SQLException e){
            e.printStackTrace();
        } finally {
            dbConnection.close();
        }

        if (result==null) 
        {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Something went wrong.").build();
        } else 
        {
            return request.createResponseBuilder(HttpStatus.OK).body("El dato fue regisrado.").build();
        }
    }
}