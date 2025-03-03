package br.com.everlyr.todolist.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;



@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired //gerenciar e instanciar o ciclo de vida
   private UserRepository userRepository;



    @PostMapping("/")
    public ResponseEntity create(@RequestBody UserModel userModel) {
        var user = this.userRepository.findByUsername(userModel.getUsername());
        if(user != null) {

            //mensagem de erro
            //status code
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário já existe");
        }
            //criptografar senha
       var passwordHash =  BCrypt.withDefaults()
               .hashToString(12,userModel.getPassword().toCharArray());
        userModel.setPassword(passwordHash);

        var userCreated =  this.userRepository.save(userModel);
         return ResponseEntity.status(HttpStatus.CREATED).body(userCreated);
    }

}
