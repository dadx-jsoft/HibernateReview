package query.criteria;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import entities.User;
import entities.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import utils.HibernateUtils;

/**
 * https://gpcoder.com/6515-hibernate-criteria-query-language-hcql/
 * 
 * @author Duong Xuan Da
 * @date 2021-11-21
 */
public class CriteriaDemo {

    // session -> criteria builder -> criteria query -> root (query.from), select
    // (query.select(root)),
    // where (query.where) -> session.createQuery(query)

    public static void main(String[] args) {
        try (Session session = HibernateUtils.getSessionFactory().openSession();) {
            // Begin a unit of work
            session.beginTransaction();

            // 1. Lấy 1 đối tượng
            CriteriaBuilder builder1 = session.getCriteriaBuilder();

            CriteriaQuery<User> query1 = builder1.createQuery(User.class);
            Root<User> root1 = query1.from(User.class); // FROM User u
            query1.select(root1); // SELECT
            query1.where(builder1.equal(root1.get("id"), 1)); // WHERE u.id = 1
            User user1 = session.createQuery(query1).uniqueResult();

            // 2. Lấy danh sách với paging và ordering
            // Ví dụ lấy danh sách user có id >= 1 và id <= 1000. Sắp xếp danh sách này theo
            // ngày tạo giảm dần, tên tăng dần. Lấy từ 5 user bắt đầu từ vị trí thứ 10.
            CriteriaBuilder builder2 = session.getCriteriaBuilder();
            CriteriaQuery<User> query2 = builder2.createQuery(User.class);
            Root<User> root2 = query2.from(User.class); // FROM
            query2.select(root2); // SELECT
            // WHERE id >= 1 AND id <= 1000
            query2.where(builder2.and(builder2.ge(root2.get("id"), 1), builder2.le(root2.get("id"), 1000)));
            // ORDER BY createdAt DESC , fullname ASC
            query2.orderBy(builder2.desc(root2.get("createdAt")), builder2.desc(root2.get("fullname")));
            List<User> users2 = session.createQuery(query2) //
                    .setFirstResult(10) // first
                    .setMaxResults(5) // max
                    .getResultList();

            // 3. Lấy một column (Selecting an expression)
            CriteriaBuilder builder3 = session.getCriteriaBuilder();
            CriteriaQuery<String> query3 = builder3.createQuery(String.class);
            Root<User> root3 = query3.from(User.class); // FROM
            query3.select(root3.get("fullname")); // SELECT fullname
            List<String> fullnames3 = session.createQuery(query3).getResultList();

            // 4. Lấy nhiều column (Selecting multiple values)
            CriteriaBuilder builder4 = session.getCriteriaBuilder();
            CriteriaQuery<Object[]> query4 = builder4.createQuery(Object[].class);
            Root<User> root4 = query4.from(User.class); // FROM
            query4.multiselect(root4.get("fullname"), root4.get("username")); // SELECT fullname, username
            List<Object[]> users4 = session.createQuery(query4).getResultList();

            // 5. Lấy nhiều column sử dụng Wrapper (Selecting a wrapper)
            // Thay vì trả về list Object[], chúng ta có thể trả về List POJO class như sau:
            // + Tạo class mới, chứa các cột trả về.
            // + Sử dụng phương thức construct của CriteriaBuilder để gán giá trị tương ứng
            // cho wrapper class.
            // Chẳng hạn, cần lấy 2 column là fullname và username
            CriteriaBuilder builder5 = session.getCriteriaBuilder();
            CriteriaQuery<BaseUser> query5 = builder5.createQuery(BaseUser.class);
            Root<User> root5 = query5.from(User.class); // FROM
            // SELECT fullname, username
            query5.select(builder5.construct(BaseUser.class, root5.get("fullname"), root5.get("username")));
            List<BaseUser> users5 = session.createQuery(query5).getResultList();

            // 6. Sử dụng hàm tập hợp (Aggregate Functions)
            // Ví dụ đếm số lượng user được tạo theo tháng.
            CriteriaBuilder builder6 = session.getCriteriaBuilder();
            CriteriaQuery<Object[]> query6 = builder6.createQuery(Object[].class);
            Root<User> root6 = query6.from(User.class);
            Expression<Long> groupByExp = builder6.function("month", Long.class, root6.get("createdAt")).as(Long.class);
            Expression<Long> countExp = builder6.count(root6.get("id"));
            query6.multiselect(groupByExp, countExp);
            query6.groupBy(groupByExp);
            query6.having(builder6.gt(builder6.count(root6), 3));
            // ordering by count in descending order
            query6.orderBy(builder6.desc(countExp));

            // 7. Truy vấn nhiều bảng (join)
            // Ví dụ: lấy thông tin user và user profile.
            CriteriaBuilder builder7 = session.getCriteriaBuilder();
            CriteriaQuery<User> query7 = builder7.createQuery(User.class);
            Root<User> root7 = query7.from(User.class); // FROM
            Join<User, UserProfile> userJoin = root7.join("userProfile", JoinType.LEFT);
            List<User> users7 = session.createQuery(query7).getResultList();

            // Commit the current resource transaction, writing any unflushed changes to the
            // database.
            session.getTransaction().commit();

            // Như bạn thấy, Query với Criteria khá đơn giản. Tuy nhiên, nó có một số vấn đề
            // sau chúng ta cần xem xét trước khi sử dụng:
            // + Performance issue: Chúng ta không có cách nào để kiểm soát truy vấn SQL do
            // Hibernate tạo ra, nếu truy vấn được tạo chậm, ta rất khó điều chỉnh truy vấn.
            // + Maintenace issue: Tất cả các truy vấn SQL được phân tán thông qua mã code
            // Java, khi một truy vấn bị lỗi, có thể dành thời gian để tìm truy vấn gây ra
            // vấn đề trong ứng dụng của mình.

            /*
             * Không có gì là hoàn hảo, hãy xem xét nhu cầu dự án của mình và sử dụng nó một
             * cách phù hợp. Đó cũng là một trong những lý dó mà Hibernate support nhiều
             * loại truy vấn khác nhau.
             */
        }
    }

}

@Data
@AllArgsConstructor
class BaseUser {
    private String fullname;
    private String username;
}
