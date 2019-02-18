public class Debugger{
    private static boolean enable = false;

    public static boolean isEnabled(){
        return enable;
    }

    public static void log(Object object){
        System.out.println(object.toString());
    }
}