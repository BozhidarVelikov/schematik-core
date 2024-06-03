package org.schematik.data.hibernate.test;

import org.schematik.data.CompareOperator;
import org.schematik.data.Query;
import org.schematik.data.QueryResult;
import org.schematik.data.transaction.Bundle;

import java.time.LocalDate;
import java.util.*;

public class HibernateTest {
    public static void test() {
        // Test Select and Delete
        Bundle.runWithNewBundle(HibernateTest::clearTables);

        // Test Create and Select
        Bundle.runWithNewBundle(bundle -> {
            Permission permission1 = new Permission();
            permission1.setCode("LOGIN_PERMISSION");
            permission1.setDescription("Permission to login.");
            bundle.add(permission1);

            Permission permission2 = new Permission();
            permission2.setCode("LOGOUT_PERMISSION");
            permission2.setDescription("Permission to logout.");
            bundle.add(permission2);

            Permission permission3 = new Permission();
            permission3.setCode("SUPER_USER_PERMISSION");
            permission3.setDescription("A permission that replaces all other permissions.");
            bundle.add(permission3);

            System.out.println(permission1);
            System.out.println(permission2);
            System.out.println(permission3);

            User user1 = new User();
            user1.setUsername("username1");
            user1.setPassword("password1");
            user1.setRegisterDate(LocalDate.now());
            user1.setPermissions(new HashSet<>(Arrays.asList(permission1, permission2)));

            User user2 = new User();
            user2.setUsername("username2");
            user2.setPassword("password2");
            user2.setRegisterDate(LocalDate.now());
            user2.setPermissions(new HashSet<>(Collections.singletonList(permission3)));

            bundle.add(user1);
            bundle.add(user2);

            Query<User> selectUsersQuery = Query.make(User.class)
                    .or(orCriterion -> orCriterion
                            .contains("permissions", permission1)
                            .contains("permissions", permission3)
                    )
                    .between(
                            "registerDate",
                            LocalDate.now().minusMonths(1),
                            LocalDate.now().plusMonths(1)
                    );

            long expectedNumberOfUsers = selectUsersQuery.count();

            QueryResult<User> selectedUsersQueryResult = selectUsersQuery.select();
            List<User> selectedUsersList = selectedUsersQueryResult.getResults();

            System.out.println("Expected users: " + expectedNumberOfUsers);
            System.out.println("Actual users: " + selectedUsersQueryResult.count());
            selectedUsersList.forEach(selectedUser -> System.out.println(selectedUser.toString()));
        });

        // Test Select and Update
        Bundle.runWithNewBundle(bundle -> {
            Query<Permission> selectPermissionQuery = Query.make(Permission.class)
                    .compare("code", CompareOperator.EQUALS, "LOGIN_PERMISSION");
            QueryResult<Permission> selectPermissionQueryResult = selectPermissionQuery.select();
            Permission selectedPermission = selectPermissionQueryResult.ensureSingleResult();

            Query<User> selectUsersQuery = Query.make(User.class)
                    .compare("username", CompareOperator.EQUALS, "username1");
            QueryResult<User> selectedUserQueryResult = selectUsersQuery.select();
            User selectedUser = selectedUserQueryResult.ensureSingleResult();
            bundle.add(selectedUser);

            selectedUser.setPermissions(new HashSet<>(Collections.singletonList(selectedPermission)));
        });
    }

    private static void clearTables(Bundle bundle) {
        Query<User> selectUsersQuery = Query.make(User.class);
        QueryResult<User> selectedUsersQueryResult = selectUsersQuery.select();
        List<User> selectedUsers = selectedUsersQueryResult.getResults();

        selectedUsers.forEach(bundle::remove);

        Query<Permission> selectPermissionsQuery = Query.make(Permission.class);
        QueryResult<Permission> selectedPermissionsQueryResult = selectPermissionsQuery.select();
        List<Permission> selectedPermissions = selectedPermissionsQueryResult.getResults();

        selectedPermissions.forEach(bundle::remove);
    }
}
