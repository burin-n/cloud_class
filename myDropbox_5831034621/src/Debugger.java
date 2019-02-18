public class Debugger{
    private static boolean enable = true;

    public static boolean isEnabled(){
        return enable;
    }

    public static void log(Object object){
        System.out.println(object.toString());
    }
}