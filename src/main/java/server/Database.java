/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.File;
import java.sql.*;
import java.util.Random;
 
/**
 * Class convert MySQL queries, creating tables, adding users, groups. Connecting in AGH website
 * @see <a href="http://mysql.agh.edu.pl">http://mysql.agh.edu.pl</a> 
 * @author Pawel Jaroch
 * @see server.User User
 * @version 1.0
 *
 */

public class Database{
    private Connection connection = null;
    private Statement statement = null;
    private ResultSet resultSet = null;
    
    
    /**
     * Method connecting in MySQL database
     * 
     */
    public void connect(){
        try {
        	
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://mysql.agh.edu.pl:3306/jaroch", "jaroch", "gH1jhCRP");
            
        }catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
          }catch(Exception e){e.printStackTrace();}	
        }
    
    /**
     * Method create tables for users, groups, files if not exist
     */
    public void createtableuser(){
    	Statement stmt = null;
    	try {
			stmt = connection.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS `users` ("
					+ " `id` int(1) NOT NULL AUTO_INCREMENT,"
					+ " `username` varchar(10) NOT NULL,"
					+ " `password` varchar(45) NOT NULL,"
					+ " `salt` varchar(20) NOT NULL,"
					+ "  PRIMARY KEY  (`id`),"
					+ " UNIQUE KEY `username` (`username`)"
					+ " ) ENGINE=MyISAM  DEFAULT CHARSET=latin2 AUTO_INCREMENT=3; ";
			stmt.addBatch(sql);
		    sql = "CREATE TABLE IF NOT EXISTS `groups` ("
					+ " `id` int(1) NOT NULL AUTO_INCREMENT,"
					+ " `group` varchar(10) NOT NULL,"
					+ " PRIMARY KEY  (`id`),"
					+ " UNIQUE KEY `group` (`group`)"
					+ "	) ENGINE=MyISAM  DEFAULT CHARSET=latin2 AUTO_INCREMENT=2; ";	
			stmt.addBatch(sql);
			sql = "CREATE TABLE IF NOT EXISTS `files` ("
					+ " `id` int(11) NOT NULL AUTO_INCREMENT,"
					+ " `filename` varchar(50) NOT NULL,"
					+ " `owner_id` int(11) NOT NULL,"
					+ " `group_id` int(11) NOT NULL,"
					+ " `user_read` tinyint(1) NOT NULL DEFAULT '1',"
					+ " `user_write` tinyint(1) NOT NULL DEFAULT '1',"
					+ " `group_read` tinyint(1) NOT NULL DEFAULT '0',"
					+ " `group_write` tinyint(1) NOT NULL DEFAULT '0',"
					+ " PRIMARY KEY  (`id`),"
					+ " UNIQUE KEY `filename` (`filename`)"
					+ " ) ENGINE=MyISAM  DEFAULT CHARSET=latin2 AUTO_INCREMENT=2; ";
			stmt.addBatch(sql);
			sql = "CREATE TABLE IF NOT EXISTS `usergroup` ("
					+ " `user_id` int(11) NOT NULL,"
					+ "`group_id` int(11) NOT NULL"
					+ ") ENGINE=MyISAM DEFAULT CHARSET=latin2; ";
			stmt.addBatch(sql);
			stmt.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Method adding new user to database
     * 
     * @param username user login in ftp
     * @param password user password in ftp
     * @return true if adding will success
     */
    public boolean addUser(String username, String password){
        connect();
        createtableuser();
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < 20; i++) {
            int a = r.nextInt(10);
            sb.append(a);
        }
        String salt = sb.toString();
        String query = "INSERT INTO `users`(`username`, `password`, `salt`) VALUES ('"+username+"',PASSWORD(CONCAT(PASSWORD('"+password+"'),'"+salt+"')),'"+salt+"')";
        
        try{
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch(SQLException e){
            e.printStackTrace(System.out);
            return false;
        }
        return true;
        
    }
    
    /**
     * Method adding new group to database
     * 
     * @param groupname users group in ftp
     * @return true if adding will success
     */
    public boolean addGroup(String groupname){
        connect();
        createtableuser();
        String query = "INSERT INTO `groups` (`group`) VALUES ('" + groupname + "');";
        
        try{
        	statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch(SQLException e){
            e.printStackTrace(System.out);
            return false;
        }
        return true;
    }
    
    /**
     * Method adding user to specify group
     * 
     * @param username user login in ftp
     * @param groupname users group in ftp
     * @return true if adding will success
     */
    public boolean addtoGroup(String username, String groupname){
    	connect();
        createtableuser();
        String query = "INSERT INTO `usergroup`(`user_id`,`group_id`) VALUES ((SELECT `id` FROM `users` WHERE `username`='"+username+"'),(SELECT `id` FROM `groups` WHERE `group`='"+groupname+"'))";
        try{
        	statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch(SQLException e){
            e.printStackTrace(System.out);
            return false;
        }
        return true;
    }
    
    /**
     * Method check is new group to add exist
     * 
     * @param groupname users group in ftp
     * @return true if is found one group, false when group doesn't exist
     */
    public boolean checkGroup(String groupname){
        connect();
        createtableuser();
        int count = 0;
        String query = "SELECT COUNT(*) FROM `groups` WHERE `group` = '"+groupname+"';";
        System.out.println(query);
        try{
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            resultSet.next();
            count = resultSet.getInt(1);
        } catch(SQLException e){
            e.printStackTrace(System.out);
            return false;
        }
        if(count == 1){
            return true;
        }
        else{
            return false;
        }
        
    }
    
    /**
     * Method check is new user to add exist
     * 
     * @param username user login in ftp
     * @param password user password in ftp
     * @return true if is found one user, false when user doesn't exist
     */
    public boolean checkUser(String username, String password){
        connect();
        createtableuser();
        int count = 0;
        String salt = null;
        String getSaltQuery = "SELECT `salt` FROM `users` WHERE username = '"+username+"';";
        try{
            statement = connection.createStatement();
            resultSet = statement.executeQuery(getSaltQuery);
            resultSet.next();
            salt = resultSet.getString("salt");
        } catch(SQLException e){
           // e.printStackTrace(System.out);
            return false;
        }
        
        String query = "SELECT count(*) FROM `users` WHERE username = '"+username+"' and password = PASSWORD(CONCAT(PASSWORD('"+password+"'),'"+salt+"'));";
        try{
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            resultSet.next();
            count = resultSet.getInt(1);
        } catch(SQLException e){
           // e.printStackTrace(System.out);
            return false;
        }
        if(count == 1){
            return true;
        }
        else{
            return false;
        }
    }
    
    /**
     * Method gets a specify user, get all information belongs group
     * 
     * @param name user login in ftp
     * @return specify user
     * @see server.User
     */
    public User getUser(String name){
        connect();
        int userId = 0, groupId = 0;
        String group = "";
        String getIdQuery = "SELECT `id` FROM `users` WHERE username = '"+name+"';";
        try{
            statement = connection.createStatement();
            resultSet = statement.executeQuery(getIdQuery);
            resultSet.next();
            userId = Integer.parseInt(resultSet.getString("id"));
        } catch(SQLException e){
            e.printStackTrace(System.out);
        }
        
        System.out.println(userId);
        String getGroupIdQuery = "SELECT `group_id` FROM `usergroup` WHERE user_id = '"+userId+"';";
        try{
            statement = connection.createStatement();
            resultSet = statement.executeQuery(getGroupIdQuery);
            resultSet.next();
            groupId = Integer.parseInt(resultSet.getString("group_id"));
        } catch(SQLException e){
            e.printStackTrace(System.out);
        }
        
        String getGroupQuery = "SELECT `group` FROM `groups` WHERE `id` = '"+groupId+"';";
        try{
            statement = connection.createStatement();
            resultSet = statement.executeQuery(getGroupQuery);
            resultSet.next();
            group = resultSet.getString("group");
        } catch(SQLException e){
            e.printStackTrace(System.out);
        }
        return new User(name,group,userId,groupId);
    }
    
    /**
     * Method adding new file to database
     * 
     * @param name filename
     * @param ownerId owner's ID
     * @param groupId specify group ID
     */
    public void addFile(String name, int ownerId, int groupId){
        connect();
        String addFileQuery;
        addFileQuery = "INSERT INTO `files`(`filename`, `owner_id`, `group_id`, `user_read`, `user_write`, `group_read`, `group_write`)"
                + " VALUES ('"+ name +"','"+ ownerId +"','"+ groupId +"',TRUE,TRUE,TRUE,FALSE)";
        try{
            statement = connection.createStatement();
            statement.executeUpdate(addFileQuery);
        } catch(SQLException e){
            e.printStackTrace(System.out);
        }
    }
    
    /**
     * Method checking access file 
     * 
     * @param f file to check
     * @return nuber of access for file
     */
    public String checkAccess(File f){
        connect();
        int userRead, userWrite, groupRead, groupWrite;
        int userAcc = 0;
        int groupAcc = 0;
        String fileQuery = "SELECT * FROM files WHERE filename = '"+ f.toString().substring(1) +"';";
        try{
            statement = connection.createStatement();
            resultSet = statement.executeQuery(fileQuery);
            if(resultSet.next()){
                userRead = resultSet.getInt("user_read");
                userWrite = resultSet.getInt("user_write");
                groupRead = resultSet.getInt("group_read");
                groupWrite = resultSet.getInt("group_write");
                userAcc = userRead + 2 * userWrite;
                groupAcc = groupRead + 2 * groupWrite;
            }
        } catch(SQLException e){
            e.printStackTrace(System.out);
        }
        return userAcc + "" + groupAcc;
    }
    
    /**
     *Method check is file for specify user can be read 
     *
     * @param u user in {@link server.User}
     * @param f file to check
     * @return true if will be read
     */
    public boolean canRead(User u, File f){
        connect();
        String canReadQuery;
        canReadQuery = "SELECT `group_read` OR (`user_read` AND `owner_id`='"+u.getId()+"') FROM `files` AS `f` WHERE `f`.`filename`='"+f.toString().substring(1)+"' AND `f`.`group_id` IN (SELECT `group_id` FROM `usergroup` WHERE `user_id`='"+u.getId()+"')";
        try{
            statement = connection.createStatement();
            resultSet = statement.executeQuery(canReadQuery);
            if(resultSet.next()){
                return resultSet.getBoolean(1);
            }
            else return false;
        } catch(SQLException e){
            e.printStackTrace(System.out);
        }
        return false;
    }
    
    /**
     *Method check is file for specify user can be write 
     *
     * @param u user in {@link server.User}
     * @param f file to check
     * @return true if will be write
     */
    public boolean canWrite(User u, File f){
        connect();
        String canWriteQuery;
        canWriteQuery = "SELECT `group_write` OR (`user_write` AND `owner_id`='"+u.getId()+"') FROM `files` AS `f` WHERE `f`.`filename`='"+f.toString().substring(1)+"' AND `f`.`group_id` IN (SELECT `group_id` FROM `usergroup` WHERE `user_id`='"+u.getId()+"')";
        try{
            statement = connection.createStatement();
            resultSet = statement.executeQuery(canWriteQuery);
            if(resultSet.next()){
                return resultSet.getBoolean(1);
            }
            else return false;
        } catch(SQLException e){
            e.printStackTrace(System.out);
        }
        return false;
    }
    
    /**
     * Method gets owner specify group
     * 
     * @param f file to check
     * @return concrete owner
     */
    public String getOwner(File f){
        connect();
        String getOwnerQuery, owner = "";
        getOwnerQuery = "SELECT `username` FROM `users` WHERE `id`= (SELECT `owner_id` FROM `files` WHERE `filename` = '"+f.toString().substring(1)+"');";
        try{
            statement = connection.createStatement();
            resultSet = statement.executeQuery(getOwnerQuery);
            if(resultSet.next()){
                owner = resultSet.getString(1);
            }
        } catch(SQLException e){
            e.printStackTrace(System.out);
        }
        return owner;
    }
    
    /**
     * Get group which match to specify file
     * 
     * @param f file to check
     * @return concrete group
     */
    public String getGroup(File f){
        connect();
        String group = "";
        String groupQuery = "SELECT `group` FROM `groups` WHERE `id`= (SELECT `group_id` FROM `files` WHERE `filename` = '"+f.toString().substring(1)+"');";
        try{
            statement = connection.createStatement();
            resultSet = statement.executeQuery(groupQuery);
            if(resultSet.next()){
                group = resultSet.getString(1);
            }
        } catch(SQLException e){
            e.printStackTrace(System.out);
        }
        return group;
    }
}

/**
 * Helpful class to store all information for cncrete user
 * 
 * @author Pawel Jaroch
 *
 */
class User {
    protected String name, group;
    protected int id, groupId;
    
    public User(String name, String group, int id, int groupId){
        this.name = name;
        this.group = group; 
        this.id = id;
        this.groupId = groupId;
    }
    
    public boolean canRead(File f){
        Database db = new Database();
        return db.canRead(this, f);
    }
    
    public boolean canWrite(File f){
        Database db = new Database();
        return db.canWrite(this, f);
    }

    /**
     * @return the user's login in ftp
     */
    public String getName() {
        return name;
    }

    /**
     * @return concrete user id
     */
    public int getId() {
        return id;
    }

    /**
     * @return concrete group Id
     */
    public int getGroupId() {
        return groupId;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(){
        return name + ", id: " + id + ", group: " + group + ", group id: " + groupId;
    }
}