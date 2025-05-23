package com.auth;

import com.accounts.AccountService;
import com.ibm.websphere.security.jwt.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

@RequestScoped
public class JwtService {

    public static String buildJwt(String id) {
        try {
            AccountService accountService = CDI.current().select(AccountService.class).get();

            ObjectId objectId = new ObjectId();

            try {
                objectId = new ObjectId(id);
            } catch (Exception e) {
                System.out.println(e);
            }

            Document user = accountService.getAccountCollection().find(eq("_id", objectId)).first();

            System.out.println("user from jwt builder" + user);

            String[] groups;
            if (user == null) {
                groups = new String[] { "user" };
            } else if (user.getInteger("admin") == 1) {
                groups = new String[] { "admin" };
            } else {
                groups = new String[] { "user" };
            }

            return JwtBuilder.create("defaultJwtBuilder")
                    .claim(Claims.SUBJECT, id)
                    .claim("groups", groups)
                    .buildJwt()
                    .compact();
        } catch (JwtException | InvalidClaimException | InvalidBuilderException e) {
            throw new RuntimeException(e);
        }
    }

}
