package com.grupo3.betting.api.recharge;

import com.grupo3.betting.api.account.UserAccountRepository;
import com.grupo3.betting.api.notification.AccountEmailNotifier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.grupo3.betting.contracts.notification.EmailNotification;
import java.util.UUID;

@Service
class RechargeService {
    private final UserAccountRepository users;
    private final RechargeRepository recharges;
    private final AccountEmailNotifier emails;

    RechargeService(UserAccountRepository users, RechargeRepository recharges, AccountEmailNotifier emails) {
        this.users = users;
        this.recharges = recharges;
        this.emails = emails;
    }

    @Transactional
    RechargeView recharge(String userId, RechargeRequest request) {
        String number = request.cardNumber().replace(" ", "");
        if (!validCard(number)) throw new IllegalArgumentException("El número debe contener exactamente 16 cifras");
        var user = users.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        user.credit(request.amount());
        var transaction = recharges.save(new RechargeTransaction(userId, request.amount(),
                request.cardholder().trim(), number.substring(number.length() - 4)));
        RechargeView view = transaction.view(user.balance());
        emails.send("notify.email.recharge", EmailNotification.recharge(UUID.randomUUID().toString(),
                user.email(), user.id(), view.reference(), view.amount(), view.cardLast4(),
                view.balance(), view.processedAt()));
        return view;
    }

    static boolean validCard(String number) {
        return number.matches("[0-9]{16}");
    }
}
