import util.HibernateUtil;

import java.util.Scanner;

public class UserServiceApplication {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserDAO userDAO = new UserDAO(HibernateUtil.getSessionFactory());
        UserService userService = new UserService(userDAO);
        ConsoleMenu consoleMenu = new ConsoleMenu(scanner, userService);
        consoleMenu.run();
    }
}
