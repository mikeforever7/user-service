package util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@Slf4j
public class HibernateUtil {

    @Getter
    private static final SessionFactory sessionFactory;

    static {
        try {
            log.info("Инициализация Hibernate SessionFactory");
            sessionFactory = new Configuration()
                    .configure()
                    .addAnnotatedClass(User.class)
                    .buildSessionFactory();
            log.info("Hibernate SessionFactory успешно создан");
        } catch (Throwable e) {
            log.error("Ошибка при создании Hibernate SessionFactory", e);
            throw new ExceptionInInitializerError(e);
        }
    }
}