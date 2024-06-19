
update users set password = '$2a$10$jfGpG/nQzkQ6VY2vk06OeOlw8C1qCEcnIFJvc6hlxn1jSJGhp56Ba' where email = 'ekalosha@gmail.com';
update users set password = '$2a$10$jfGpG/nQzkQ6VY2vk06OeOlw8C1qCEcnIFJvc6hlxn1jSJGhp56Ba' where email = 'admin@email.com';
-- insert into user_roles (user_id, role_id) (select u.id as user_id, 1 as role_id from users u where u.email = 'ekalosha@gmail.com');
