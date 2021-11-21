package query.hql;

import java.util.List;

import javax.persistence.Query;

import org.hibernate.Session;

import entities.User;
import utils.HibernateUtils;

/**
 * https://gpcoder.com/6502-hibernate-query-language-hql/#Gioi_thieu_Hibernate_Query_Language_HQL
 * 
 * @author Duong Xuan Da
 * @date 2021-11-21
 */

public class HQLDemo {
    public static void main(String[] args) {
        try (Session session = HibernateUtils.getSessionFactory().openSession();) {
            // Begin a unit of work
            session.beginTransaction();

            // HQL

            // 1. Ví dụ mệnh đề FROM
            // Lấy danh sách User:
            String hql_1_1 = "FROM User";
            List<User> users_1 = session.createQuery(hql_1_1, User.class).list();

            // Lấy 1 user theo id là 1
            String hql_1_2 = "FROM User AS u WHERE u.id = :id";
            User user_1 = session.createQuery(hql_1_2, User.class).setParameter("id", 1L).uniqueResult();

            // 2. Ví dụ mệnh đề SELECT
            // Nếu không có mệnh đề SELECT, HQL sẽ lấy tất cả các field của Entity.
            // Chúng ta có thể giới hạn số lượng cột cần lấy sử dụng mệnh đề SELECT.

            // Ví dụ lấy username của 1 user
            String hql_2_1 = "SELECT u.username FROM User u WHERE u.id = :id";
            String username_1 = session.createQuery(hql_2_1, String.class).setParameter("id", 1L).uniqueResult();

            // 3. Ví dụ mệnh đề WHERE
            // Ví dụ: lấy danh sách user được tạo trong tháng hiện tại:
            String hql_3 = "FROM User u WHERE month(u.createdAt) = month(sysdate())";
            List<User> users_3 = session.createQuery(hql_3, User.class).list();

            // 4. Ví dụ mệnh đề ORDER BY
            // Ví dụ: lấy danh sách user được tạo trong tháng hiện tại và sắp xếp ngảy tạo
            // giảm dần,
            // username tăng dần.
            String hql_4 = "FROM User u WHERE month(u.createdAt) = month(sysdate()) "
                    + " ORDER BY u.createdAt DESC, u.username ASC";
            List<User> users_4 = session.createQuery(hql_4, User.class).list();

            // 5. Ví dụ mệnh đề GROUP BY
            // Ví dụ đếm số lượng user được tạo theo mỗi tháng của năm hiện tại.
            String hql_5 = "SELECT month(createdAt) AS month, COUNT(id) AS numberOfUser FROM User "
                    + " WHERE year(createdAt) = year(sysdate()) GROUP BY month(createdAt) HAVING COUNT(id) > 3";
            List<Object[]> result_5 = session.createQuery(hql_5).list();

            // 6. Ví dụ mệnh đề UPDATE
            // Ví dụ: update thông tin fullname và password của user có id là 1.
            String hql_6 = "UPDATE User SET fullname = :fullname, password = :password WHERE id = :id";
            Query query_6 = session.createQuery(hql_6);
            query_6.setParameter("fullname", "GP CODER");
            query_6.setParameter("password", "gpcoder.com");
            query_6.setParameter("id", 1L);
            int affectedRows_6 = query_6.executeUpdate();

            // 7. Ví dụ mệnh đề DELETE
            // Ví dụ: xoá tất cả user được tạo trong tháng 2.
            String hql_7 = "DELETE FROM User WHERE month(createdAt) = :month";
            Query query_7 = session.createQuery(hql_7);
            query_7.setParameter("month", 2);
            int affectedRows_7 = query_7.executeUpdate();

            // 8. Ví dụ mệnh đề INSERT INTO
            // Ví dụ: copy tất cả user đang có và insert vào bảng user với user có prefix là
            // "copyOf".
            String hql_8 = "INSERT INTO User(fullname, username, password, createdAt, modifiedAt) "
                    + " SELECT fullname, CONCAT('copyOf', username) , password, sysdate(), sysdate() FROM User";
            Query query_8 = session.createQuery(hql_8);
            int affectedRows_8 = query_8.executeUpdate();

            // 9. Ví dụ về phân trang
            // Có hai phương thức của giao tiếp Query cho việc phân trang:
            // setFirstResult(int startPosition): xác định hàng đầu tiên trong tập kết quả
            // (start with 0).
            // setMaxResults(int maxResult): xác định số lượng hàng cần lấy.

            // Ví dụ: lấy 10 user bắt đầu từ user có thứ tự thứ 5.
            String hql_9 = "FROM User";
            Query query_9 = session.createQuery(hql_9, User.class);
            query_9.setFirstResult(5);
            query_9.setMaxResults(10);
            List<User> users_9 = query_9.getResultList();

            // 10. Ví dụ về JOIN
            // Ví dụ: lấy thông tin user và user profile.
            // Do thông tin profile có thể có hoặc không, nên chúng ta sẽ sử dụng LEFT JOIN.
            String hql = "FROM User u LEFT JOIN u.userProfile p WHERE u.id = :id";
            List<Object[]> users_10 = session.createQuery(hql).setParameter("id", 15L).list();

            session.getTransaction().commit();
        }
    }
}
