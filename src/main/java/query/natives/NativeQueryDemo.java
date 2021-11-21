package query.natives;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.hibernate.transform.Transformers;

import entities.User;
import entities.UserProfile;
import lombok.Data;
import utils.HibernateUtils;

/**
 * https://gpcoder.com/6536-hibernate-native-sql-queries/#Handling_associations_and_collections
 * 
 * @author Duong Xuan Da
 * @date 2021-11-21
 */
public class NativeQueryDemo {
    public static void main(String[] args) {

        try (Session session = HibernateUtils.getSessionFactory().openSession();) {
            // Begin a unit of work
            session.beginTransaction();
            

            // 1. Scalar queries
            // Các truy vấn SQL cơ bản nhất là để có được một danh sách các giá trị scalars
            // (column) từ một hoặc nhiều bảng.
            // 1.1 Ví dụ lấy tất cả các column của bảng user.
            NativeQuery query11 = session.createNativeQuery("SELECT * FROM user");
            List<Object[]> users11 = query11.getResultList();

            // 1.2 Ví dụ lấy fullname và email của 10 user từ vị trí thứ 5
            NativeQuery query12 = session.createNativeQuery("SELECT username, password FROM user ORDER BY username");
            List<Object[]> users12 = query12.setFirstResult(5).setMaxResults(10).getResultList();
            users12.forEach(user -> {
                System.out.println("username: " + user[0] + " | password: " + user[1]);
            });

            // 1.3 Ví dụ lấy username và password của một user có id là 1
            NativeQuery query13 = session.createNativeQuery("SELECT username, password FROM user where id = :id");
            Object[] user13 = (Object[]) query13.setParameter("id", 1).uniqueResult();
            System.out.println("username: " + user13[0] + " | password: " + user13[1]);

            // 2. Entity queries
            // Các truy vấn ở trên là về trả về các giá trị vô hướng từ ResultSet.
            // 2.1 Ví dụ sử dụng JPA native query để có được các đối tượng Entity từ một
            // truy vấn Native SQL
            NativeQuery query21 = session.createNativeQuery("SELECT * FROM user", User.class);
            List<User> users21 = query21.getResultList();
            // 2.2 Sau đây là cú pháp để có được các đối tượng Entity từ một truy vấn Native
            // SQL thông qua addEntity().
            NativeQuery query22 = session.createNativeQuery("SELECT * FROM user");
            List<User> users22 = query22.addEntity(User.class).list();

            // 3. DTOs (Data Transfer Objects)
            // Đôi khi chúng ta cần truy vấn một vài column và trả về trực tiếp cho đối
            // tượng DTO, không cần phải thông qua Entity.

            // 3.1 Sử dụng ResultTransformer để trả về đượng tượng non-entity.
            NativeQuery query31 = session.createNativeQuery("SELECT fullname, username FROM user");
            List<UserDTO> users31 = query31.setResultTransformer(Transformers.aliasToBean(UserDTO.class)).list();

            // 3.2 Nếu không muốn trả về DTO, có thể trả về một Map<String, Object> như sau:
            NativeQuery query = session.createNativeQuery("SELECT fullname, username FROM user");
            List<Map<String, Object>> mapUsers = query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE)
                    .list();
            mapUsers.forEach(user -> {
                System.out.println(user.get("fullname"));
            });

            // 4. Handling associations and collections
            // Ví dụ lấy thông tin user và user profile.
            List<Object[]> tuples = session.createNativeQuery( //
                    "SELECT * " + //
                            "FROM user u " + //
                            "INNER JOIN user_profile p ON u.id = p.user_id") //
                    .addEntity("user", User.class) //
                    .addJoin("p", "user.userProfile") //
                    .list();

            for (Object[] tuple : tuples) {
                User user = (User) tuple[0];
                UserProfile userProfile = (UserProfile) tuple[1];
                System.out.println("fullname " + user.getFullname() + " | address " + userProfile.getAddress());
            }
            // Mặc định khi sử dụng addJoin(), kết quả trả về sẽ bao gồm tất cả các entity
            // được join trong 1 array. Để trả về một đối tượng entity hierarchy, chúng ta
            // cần sử dụng ROOT_ENTITY hoặc DISTINCT_ROOT_ENTITY ResultTransformer.

            
            
            // Commit the current resource transaction, writing any unflushed changes to the
            // database.
            session.getTransaction().commit();
        }
    }
}

@Data
class UserDTO {
    private String fullname;
    private String username;
}
