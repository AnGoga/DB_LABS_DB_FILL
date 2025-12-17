

-------- 1 --------------

WITH RECURSIVE result AS (

    SELECT et.id, et.first_name, et.last_name, 1 as level from employees_tree et
    WHERE et.id = (
        SELECT et_.id FROM employees_tree et_
        where et_.first_name = 'Мария' AND et_.last_name = 'Сидорова'
    )

    UNION ALL

    SELECT et2.id, et2.first_name, et2.last_name, (level + 1) as level
    from employees_tree et2
    INNER JOIN result r ON r.id = et2.manager_id
)
SELECT * FROM result;



-------- 2.1 --------------
SELECT * from employees_tree

EXCEPT

SELECT et.* FROM employees_tree et
                     INNER JOIN employees_tree et2 ON et2.manager_id = et.id

    -------- 2.2 --------------
SELECT distinct et.* FROM employees_tree et
                              LEFT JOIN employees_tree et2 ON et2.manager_id = et.id
WHERE et2.manager_id IS NULL;


-------- 2.3 --------------
SELECT distinct et.* FROM employees_tree et
WHERE NOT EXISTS(
    SELECT et2.* FROM employees_tree et2
    WHERE et2.manager_id = et.id
);


-------- 3 --------------

WITH RECURSIVE result AS (

    SELECT et.id, et.first_name, et.last_name, et.manager_id from employees_tree et
    WHERE et.id = (
        SELECT et_.id FROM employees_tree et_
        where et_.first_name = 'Артем' AND et_.last_name = 'Васильев'
    )

    UNION ALL

    SELECT et2.id, et2.first_name, et2.last_name, et2.manager_id
    from employees_tree et2
    INNER JOIN result r ON r.manager_id = et2.id
)
SELECT * FROM result;





-------- 4 --------------

WITH RECURSIVE result AS (

    SELECT et.id, et.first_name, et.last_name, et.salary
    from employees_tree et
    WHERE et.id = (
        SELECT et_.id FROM employees_tree et_
        where et_.first_name = 'Мария' AND et_.last_name = 'Сидорова'
    )

    UNION ALL

    SELECT et2.id, et2.first_name, et2.last_name, et2.salary
    from employees_tree et2
    INNER JOIN result r ON r.id = et2.manager_id
)
SELECT SUM(r.salary) FROM result r;


-------- 4 --------------

WITH RECURSIVE res AS(
    SELECT * FROM employees_tree
), sub AS (
    SELECT et.id, et.first_name, et.last_name, et.salary
    from employees_tree et
    WHERE et.id = (
        SELECT et_.id FROM employees_tree et_
        where et_.first_name = 'Мария' AND et_.last_name = 'Сидорова'
    )

    UNION ALL

    SELECT et2.id, et2.first_name, et2.last_name, et2.salary
    from employees_tree et2
    INNER JOIN sub r ON r.id = et2.manager_id
)
SELECT * FROM sub r;




--------------------------------------------
--------------------------------------------
--------------------------------------------




DROP TABLE IF EXISTS orders1;

CREATE TEMPORARY TABLE orders1(
                                  status     TEXT NOT NULL,
                                  textStatus TEXT NOT NULL
);

INSERT INTO orders1(status, textStatus) VALUES
                                            ('paid', 'Оплчено'),
                                            ('cancelled', 'Отменено'),
                                            ('failed', 'Что-то пошло не так'),
                                            ('new', 'Новое');

SELECT * FROM orders o
                  LEFT JOIN orders1 o2 on o.status = o2.status

---------------2---------------

SELECT categpry, count(categpry) FROM (SELECT o.*,
                                              CASE
                                                  when o.status = 'paid' THEN 'success'
                                                  when o.status = 'failed' or o.status = 'cancelled' THEN 'fail'
                                                  END as categpry
                                       FROM orders o)
WHERE categpry is NOT NULL
GROUP BY categpry;

---------------3-----------------

SELECT o.*,
       CASE
           when o.amount < 1000 THEN 'small'
           when o.amount >= 1000 AND o.amount < 5000 THEN 'medium'
           when o.amount >= 5000 THEN 'large'
           END as categpry
FROM orders o

    ---------------5-----------------

SELECT o FROM orders o
WHERE o.created_at


--------------- 6 -----------------

-- select o.*, CASE
--     CASE o.created_at <= (CURRENT_TIME - INTERVAL '11 month') THEN 0
--     CASE o.created_at <= (CURRENT_TIME - INTERVAL '10 month') THEN 1
--     ELSE
-- END
-- from orders o






