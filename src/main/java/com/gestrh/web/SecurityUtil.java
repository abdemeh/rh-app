package com.gestrh.web;

import jakarta.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SecurityUtil {
    private SecurityUtil(){}

    public static Set<String> loadRoles(EntityManager em, Integer userId){
        @SuppressWarnings("unchecked")
        List<String> rs = em.createNativeQuery(
                        "SELECT r.code " +
                                "FROM user_roles ur JOIN roles r ON r.id = ur.role_id " +
                                "WHERE ur.utilisateur_id = ?1")
                .setParameter(1, userId)   // <-- positional param
                .getResultList();

        return new HashSet<>(rs);
    }
}
