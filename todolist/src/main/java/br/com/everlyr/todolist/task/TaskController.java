package br.com.everlyr.todolist.task;

import br.com.everlyr.todolist.util.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.rmi.server.UID;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
   private TaskRepository taskRepository ;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        var idUser =  request.getAttribute("idUser");
        taskModel.setIdUser((UUID) idUser);

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de início/término deve ser maior que a data atual.");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de início deve ser menor que a data de término.");
        }

        // Salva a tarefa no banco de dados
        try {
            var task = this.taskRepository.save(taskModel);
            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao salvar a tarefa: " + e.getMessage());
        }
    }


    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {

        var idUser = request.getAttribute("idUser");
       var tasks =  this.taskRepository.findByIdUser((UUID) idUser);
       return tasks;
    }

     @PutMapping("/{id}")
    public ResponseEntity update (@RequestBody TaskModel taskModel, @PathVariable UUID id , HttpServletRequest request) {


         var task = this.taskRepository.findById(id).orElse(null);

         if(task == null) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                     .body("Tarefa não encontrada");
         }

         var idUser = request.getAttribute("idUser");

         if(!task.getIdUser().equals(idUser)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Usuário não tem permissão de alterar essa tarefa");
        }
         Utils.copyNonNullProperties(taskModel,task);
         var taskUpdate = this.taskRepository.save(task);

       return ResponseEntity.ok().body(taskUpdate);
     }

 }
