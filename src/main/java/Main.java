import static javax.measure.unit.SI.KILOGRAM;
import javax.measure.quantity.Mass;
import org.jscience.physics.model.RelativisticModel;
import org.jscience.physics.amount.Amount;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class Main extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (req.getRequestURI().endsWith("/db")) {
      showDatabase(req,resp);
    } else {
      showHome(req,resp);
    }
  }

  private void showHome(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // Energy is compatible with mass (E=mc2)
    RelativisticModel.select();

    String energy = System.getenv().get("ENERGY");

    // *** TEST ***
    java.io.PrintWriter pw = resp.getWriter();
    if (pw != null) {
      resp.getWriter().println("*** DEBUG *** Pushed this change to the github repo...");
      resp.getWriter().println("*** DEBUG *** Retrieved PrintWriter instance...");
      resp.getWriter().println("*** DEBUG *** Lunched remotely from my heroku/rfbaker200501 folder.");
    } else {
      resp.getWriter().println("*** ERROR *** Could not get PrintWriter object...");
    }

    if (energy != null) {
      Amount<Mass> m = Amount.valueOf(energy).to(KILOGRAM);
      pw.println("*** DEBUG *** Retrieved Mass instance...");
      pw.println("*** DEBUG *** energy: " + energy);
      resp.getWriter().println("E=mc^2: 12 GeV = " + m);
    } else {
      resp.getWriter().println("*** ERROR *** Could not initialize the 'engery' variable. Value of 'engery' is: [" + energy + "]");
    }
    // *** TEST ***

//    Amount<Mass> m = Amount.valueOf(energy).to(KILOGRAM);

//    resp.getWriter().print("E=mc^2: 12 GeV = " + m);
  }

  private void showDatabase(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Connection connection = null;
    try {
      connection = getConnection();

      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
      ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

      String out = "Hello!\n";
      while (rs.next()) {
          out += "Read from DB: " + rs.getTimestamp("tick") + "\n";
      }

      resp.getWriter().print(out);
    } catch (Exception e) {
      resp.getWriter().print("There was an error: " + e.getMessage());
    } finally {
      if (connection != null) try{connection.close();} catch(SQLException e){}
    }
  }

  private Connection getConnection() throws URISyntaxException, SQLException {
    URI dbUri = new URI(System.getenv("DATABASE_URL"));

    String username = dbUri.getUserInfo().split(":")[0];
    String password = dbUri.getUserInfo().split(":")[1];
    int port = dbUri.getPort();

    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + port + dbUri.getPath();

    return DriverManager.getConnection(dbUrl, username, password);
  }

  public static void main(String[] args) throws Exception {
    Server server = new Server(Integer.valueOf(System.getenv("PORT")));
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    server.setHandler(context);
    context.addServlet(new ServletHolder(new Main()),"/*");
    server.start();
    server.join();
  }
}
