package com.grupo3.betting.api.account;

import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/admin/users") @CrossOrigin(origins = "*")
class AdminController {
    private final UserAccountRepository users;
    AdminController(UserAccountRepository users) { this.users = users; }
    @GetMapping @Transactional(readOnly = true)
    List<UserView> all() { return users.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().map(UserView::from).toList(); }
    @PatchMapping("/{id}/balance") @Transactional
    UserView addBalance(@PathVariable String id, @Valid @RequestBody BalanceRequest request) { var user=require(id); user.credit(request.amount()); return UserView.from(user); }
    @PatchMapping("/{id}/role") @Transactional
    UserView role(@PathVariable String id, @Valid @RequestBody RoleRequest request) { var user=require(id); user.setRole(request.role()); return UserView.from(user); }
    @PatchMapping("/{id}/active") @Transactional
    UserView active(@PathVariable String id, @RequestBody ActiveRequest request) { var user=require(id); user.setActive(request.active()); return UserView.from(user); }
    private UserAccount require(String id) { return users.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado")); }
}
