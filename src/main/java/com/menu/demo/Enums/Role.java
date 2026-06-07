package com.menu.demo.Enums;

import java.util.Collection;
import java.util.List;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Role {
    SUPER_ADMIN, SCHOOL_ADMIN, TEACHER, STUDENT;

    public Collection<? extends GrantedAuthority> getAuthorities() {
       
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.name()));
    }
}
