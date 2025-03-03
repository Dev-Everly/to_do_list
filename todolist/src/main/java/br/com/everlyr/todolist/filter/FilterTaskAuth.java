package br.com.everlyr.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.everlyr.todolist.user.UserRepository;
import io.micrometer.core.ipc.http.HttpSender;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
@Order(1)
public class FilterTaskAuth extends OncePerRequestFilter {
    @Autowired
    UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var servlePath = request.getServletPath();

        if (servlePath.startsWith("/tasks")){
            //pegar auth
            var authorization = request.getHeader("Authorization");


            var authEnconded = authorization.substring("Basic".length()).trim();

            byte[] authDecode = Base64.getDecoder().decode(authEnconded);

            var authString = new String(authDecode);
            System.out.println("Auth");


            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];


            //validar user
            var user = this.userRepository.findByUsername(username);
            if (user == null) {
                response.sendError(401);
            } else {


                //validar password

                var passwordVerify = BCrypt.verifyer()
                        .verify(password.toCharArray(), user.getPassword());
                if (passwordVerify.verified) {
                    request.setAttribute("idUser",user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }

            }
        } else {
            filterChain.doFilter(request,response);
        }


    }
}






