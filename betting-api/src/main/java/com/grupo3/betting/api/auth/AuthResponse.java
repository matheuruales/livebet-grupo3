package com.grupo3.betting.api.auth;

import com.grupo3.betting.api.account.UserView;

record AuthResponse(String token, UserView user) {}
