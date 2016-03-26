import java.sql.*;

import com.salesucation.sparkphp.PHPRenderer;

import org.json.simple.JSONObject;

import javax.servlet.http.HttpSession;

import io.github.rhildred.*;

import static spark.Spark.*;

/**
 * Hello world!
 *
 */
public class App
{
    private static final String IP_ADDRESS = System.getenv("OPENSHIFT_DIY_IP") != null ? System.getenv("OPENSHIFT_DIY_IP") : "localhost";
    private static final int PORT = System.getenv("OPENSHIFT_DIY_PORT") != null ? Integer.parseInt(System.getenv("OPENSHIFT_DIY_PORT")) : 4567;
    public static void main(String[] args) {
        setIpAddress(IP_ADDRESS);
        setPort(PORT);
        externalStaticFileLocation(System.getProperty("user.dir") + "/public/");
        PHPRenderer php = new PHPRenderer();
        php.setViewDir("views/");
        final Connection connection = OpenShiftSQLiteSource.getConnection();
        final Oauth2 oauth = new Oauth2();
        try{
            get("/", (request, response) -> {
                String rc = "";
                String sModel = "{";
                Statement oStmt = null;
                JSONObject oInfo = oauth.getCreds(request.session().raw());
                if(oInfo != null){
                    sModel = sModel + "\"currentUser\":" + oInfo.toJSONString() + ",";
                }
                try{
                    oStmt = connection.createStatement();
                    String sSQL = "SELECT * FROM jobpostings";
                    ResultSet oRs = oStmt.executeQuery(sSQL);
                    sModel = sModel + "\"data\":" + ResultSetValue.toJsonString(oRs) + "}";
                    oRs.close();
                    rc = php.render("index.phtml", sModel);
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    try{
                        if(oStmt != null) oStmt.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                return(rc);
            });
            get("/postings/:page", (request, response) -> {
                String rc = "";
                PreparedStatement oStmt = null;
                try{
                    String sModel = "{";
                    JSONObject oInfo = oauth.getCreds(request.session().raw());
                    if(oInfo != null){
                        sModel = sModel + "\"currentUser\":" + oInfo.toJSONString() + ",";
                    }
                    String sSQL = "SELECT * FROM jobpostings WHERE id = ?";
                    oStmt = connection.prepareStatement(sSQL);
                    oStmt.setString(1, request.params(":page"));
                    ResultSet oRs = oStmt.executeQuery();
                    sModel = sModel + "\"data\":" + ResultSetValue.toJsonString(oRs) + "}";
                    oRs.close();
                    rc = php.render("page.phtml", sModel);

                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    try{
                        if(oStmt != null) oStmt.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                return rc;

            });
            get("/postings", (request, response) -> {
                String rc = "";
                try{
                    Statement oStmt = connection.createStatement();
                    String sSQL = "SELECT * FROM jobpostings";
                    ResultSet oRs = oStmt.executeQuery(sSQL);
                    rc = ResultSetValue.toJsonString(oRs);
                    oRs.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
                return rc;

            });
            get("/login/:type", (request, response) -> {
                String rc = "";
                try {
                    HttpSession sess = request.session().raw();
                    sess.setAttribute("referer", request.headers("referer"));
                    sess.setAttribute("type", request.params(":type"));
                    //get rid of :type from login url
                    oauth.redirect(request.url().replaceAll("/([^/]*)$", ""), response.raw());

                }catch(Exception e){
                    e.printStackTrace();
                }
                return rc;
            });
            get("/login", (request, response) -> {
                String rc = "";
                try {
                    HttpSession sess = request.session().raw();
                    JSONObject oInfo = oauth.handleCode(request.queryParams("code"));
                    oInfo.put("type", sess.getAttribute("type"));
                    sess.setAttribute("creds", oInfo);
                    response.redirect((String) sess.getAttribute("referer"));
                }catch(Exception e){
                    e.printStackTrace();
                }
                return rc;
            });
            get("/logout", (request, response) -> {
                String rc = "";
                HttpSession sess = request.session().raw();
                sess.removeAttribute("creds");
                response.redirect(request.headers("referer"));
                return rc;
            });
            get("/currentUser", (request, response) -> {
                JSONObject oInfo = oauth.getCreds(request.session().raw());
                if(oInfo == null){
                    halt(401, "not logged in");
                }else {
                    return oInfo.toJSONString();
                }
                // shouldn't get here
                return "";
            });
        }catch(Exception e){
        	e.printStackTrace();
        }
    }
}