
public class test {
    public static void main(String[] args) {
        // List<Map<String, Object>> results = ConnectSqLite.executeSelectAndGetResults("SELECT * FROM employees where id = ?", 1);
        // for (Map<String, Object> row : results){
        //     System.out.printf("%s\n", row.get("nom"));
        // }
        ConnectSqLite.executeDelete("delete from employees");
    }
}
