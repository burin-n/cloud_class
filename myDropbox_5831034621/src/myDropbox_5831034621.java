// add other aws imports

import java.util.Scanner;

/**
 *
 * @author Burin Naowarat
 */
public class myDropbox_5831034621 {

    private static String region = "ap-southeast-1";
    private static Controller controller =  new Controller(region);
    public static Debugger debugger = new Debugger();

    public static void main(String[] args) {

        String login_as = null;
        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.println("Waiting for command...");


            String command = sc.nextLine();
            String[] cmd = command.split(" ");


            if (cmd[0].equals("newuser")) {
                String username = cmd[1];
                String password = cmd[2];
                String password_confirm = cmd[3];

                if (password.equals(password_confirm)) {
                    if (controller.addUser(username, password)) {
                        login_as = username;
                        System.out.println("User: " + login_as + " is registered.");
                    }
                } else {
                    System.out.println("Password does not match.");
                }

            }

            else if (cmd[0].equals("login")) {
                String username = cmd[1];
                String password = cmd[2];
                if (controller.login(username, password)) {
                    login_as = username;
                    System.out.println("You are login as: " + login_as + ".");
                } else {
                    System.out.println("Username or password is incorrect.");
                }

            }

            else if (cmd[0].equals("logout")) {
                login_as = null;
                System.out.println("You are logout");

            }

            else if (cmd[0].equals("quit")) {
                break;
            }

            else if(cmd[0].equals("put") && login_as!=null) {
                String fileName = cmd[1];
                controller.put(login_as, fileName);
            }

            else if(cmd[0].equals("view") && login_as!=null) {
                controller.view(login_as);
            }

            else if(cmd[0].equals("share") && login_as!=null) {
                String fileName = cmd[1];
                String toUser = cmd[2];
                controller.share(fileName, login_as, toUser);
            }

            else if(cmd[0].equals("get") && login_as !=null){
                String fileName = cmd[1];
                String owner = login_as;
                if(cmd.length == 3) owner = cmd[2];

                controller.get(fileName, owner, login_as);
            }

        }
    }
}