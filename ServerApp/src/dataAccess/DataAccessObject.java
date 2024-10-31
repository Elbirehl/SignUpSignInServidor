package dataAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import logicalExceptions.MaxThreadsErrorException;
import logicalExceptions.ServerErrorException;
import logicalExceptions.SignInErrorException;
import logicalExceptions.UserExistErrorException;
import logicalExceptions.UserNotActiveException;
import logicalModel.interfaces.Signable;
import logicalModel.model.User;

/**
 * DataAccessObject class that manages database connections and implements the
 * Signable interface for user operations.
 *
 * This class handles the interactions with the PostgreSQL database, including
 * user registration and login functionality.
 *
 * @author Irati, Meylin, Olaia, Elbire
 */
public class DataAccessObject implements Signable {

    private Connection con;
    private PreparedStatement stmt;
    private ResultSet rs;
    private static final Logger logger = Logger.getLogger(DataAccessObject.class.getName());

    // Create an instance of the connection pool.
    private final PoolConnections pool = new PoolConnections();

    /**
     * Opens a connection to the database.
     *
     * @return a Connection object representing the database connection, or null
     * if the connection could not be established.
     */
    public Connection openConnection() {
        con = null;
        try {
            con = pool.getConnection();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error when trying to open the database: {0}", e.getMessage());
        }
        return con;
    }

    /**
     * Closes the connection to the database and releases any resources held.
     * This method closes the ResultSet, PreparedStatement, and returns the
     * Connection to the connection pool if they are open.
     */
    public void closeConnection() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                pool.returnConnection(con);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataAccessObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Signs in a user to the system.
     *
     * @param user the user to sign in
     * @return the signed-in user with updated information
     * @return the signed-in user with updated information
     * @throws MaxThreadsErrorException if the maximum number of allowed
     * concurrent threads has been exceeded
     * @throws ServerErrorException if there is an error when accessing the
     * server
     * @throws SignInErrorException if the user is not found in the database or
     * the credentials are incorrect
     * @throws UserNotActiveException if the user exists but is not active
     */
    @Override
    public User signIn(User user) throws MaxThreadsErrorException, ServerErrorException, SignInErrorException, UserNotActiveException {
        //signin
        final String USEREXISTS = "SELECT * FROM public.res_users u, public.res_partner p WHERE p.id = u.partner_id AND login=? AND password=?";
        con = openConnection();
        stmt = null;
        rs = null;

        try {
            stmt = con.prepareStatement(USEREXISTS);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswd());
            rs = stmt.executeQuery();

            if (rs.next()) {
                boolean isActive = rs.getBoolean("active");
                if (isActive == true) {
                    user = new User();
                    user.setActive(isActive);
                    user.setCity(rs.getString("city"));
                    user.setEmail(rs.getString("email"));
                    user.setMobile(rs.getInt("mobile"));
                    user.setName(rs.getString("name"));
                    user.setPasswd(rs.getString("password"));
                    user.setStreet(rs.getString("street"));
                    user.setZip(rs.getInt("zip"));
                    return user;
                } else {
                    throw new UserNotActiveException("The user is inactive.");
                }
            } else {
                throw new SignInErrorException("User not found");
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL error", e);
            throw new ServerErrorException("Error accessing the server.");
        } finally {
            closeConnection();
        }

    }

    /**
     * Registers a new user in the system.
     *
     * This method first checks if a user with the given email already exists.
     * If not, it creates a new partner in the res_partner table, retrieves the
     * partner ID, and then inserts the new user in the res_users table.
     *
     * @param user the user to sign up
     * @return the registered user with updated information or null if
     * registration fails
     * @throws logicalExceptions.ServerErrorException
     * @throws logicalExceptions.UserExistErrorException
     */
    @Override
    public User signUp(User user) throws ServerErrorException, UserExistErrorException {
        final String EMAILEXISTS = "SELECT * FROM public.res_users WHERE login = ?";
        final String INSERTPARTNER = "INSERT INTO public.res_partner(company_id, name, street, zip, city, email, active, mobile) VALUES('1',  ?,  ?,  ?,  ?,  ?, ?, ?)";
        final String SELECTPARTNER = "SELECT id FROM public.res_partner WHERE email = ?";
        final String INSERTUSER = "INSERT INTO public.res_users(company_id, partner_id, active, login, password, notification_type) VALUES('1', ?, ?, ?, ?, 'email')";

        con = openConnection();
        stmt = null;
        rs = null;
        try {
            // Check if the user already exists
            stmt = con.prepareStatement(EMAILEXISTS);
            stmt.setString(1, user.getEmail());
            rs = stmt.executeQuery();
            if (rs.next()) {
                throw new UserExistErrorException("The user is already registered.");
            }

            // Insert the new partner into the res_partner table
            stmt = con.prepareStatement(INSERTPARTNER);
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getStreet());
            stmt.setInt(3, user.getZip());
            stmt.setString(4, user.getCity());
            stmt.setString(5, user.getEmail());
            stmt.setBoolean(6, user.isActive());
            stmt.setInt(7, user.getMobile());
            stmt.executeUpdate();

            // Get the ID of the newly created partner
            stmt = con.prepareStatement(SELECTPARTNER);
            stmt.setString(1, user.getEmail());
            rs = stmt.executeQuery();
            int partnerId = -1;
            if (rs.next()) {
                partnerId = rs.getInt("id");
            }

            if (partnerId == -1) {
                throw new ServerErrorException("Error getting partner ID.");
            }

            // Insert the new user into the res_users table
            stmt = con.prepareStatement(INSERTUSER);
            stmt.setInt(1, partnerId);
            stmt.setBoolean(2, user.isActive());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPasswd());
            stmt.executeUpdate();

            logger.log(Level.INFO, "User successfully registered.");
            return user;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error registering user", e);
            throw new ServerErrorException("Error registering user");
        } finally {
            closeConnection();
        }
    }
}
