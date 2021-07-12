package com.wdd.studentmanager.controller;

import com.wdd.studentmanager.domain.Student;
import com.wdd.studentmanager.service.ClazzService;
import com.wdd.studentmanager.service.SelectedCourseService;
import com.wdd.studentmanager.service.StudentService;
import com.wdd.studentmanager.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @Classname StudentController
 * @Description None
 * @Date 2019/6/25 20:00
 * @Created by WDD
 */
@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private SelectedCourseService selectedCourseService;

    /**
     * 跳转学生列表页面
     * @return
     */
    @GetMapping("/student_list")
    public String studentList(){
        return "/student/studentList";
    }

    /**
     * 异步加载学生列表
     * @param page
     * @param rows
     * @param studentName
     * @param clazzid
     * @param from
     * @param session
     * @return
     */
    @RequestMapping("/getStudentList")
    @ResponseBody
    public Object getStudentList(@RequestParam(value = "page", defaultValue = "1")Integer page,
                                 @RequestParam(value = "rows", defaultValue = "100")Integer rows,
                                 String studentName,
                                 @RequestParam(value = "clazzid", defaultValue = "0")String clazzid, String from, HttpSession session){
        Map<String,Object> paramMap = new HashMap();
        paramMap.put("pageno",page);
        paramMap.put("pagesize",rows);
        if(!StringUtils.isEmpty(studentName))  paramMap.put("username",studentName);
        if(!clazzid.equals("0"))  paramMap.put("clazzid",clazzid);

        //判断是老师还是学生权限
        Student student = (Student) session.getAttribute(Const.STUDENT);
        if(!StringUtils.isEmpty(student)){
            //是学生权限，只能查询自己的信息
            paramMap.put("studentid",student.getId());
        }

        PageBean<Student> pageBean = studentService.queryPage(paramMap);
        if(!StringUtils.isEmpty(from) && from.equals("combox")){
            return pageBean.getDatas();
        }else{
            Map<String,Object> result = new HashMap();
            result.put("total",pageBean.getTotalsize());
            result.put("rows",pageBean.getDatas());
            return result;
        }
    }

    /**
     * 删除学生
     * @param data
     * @return
     */
    @PostMapping("/deleteStudent")
    @ResponseBody
    public AjaxResult deleteStudent(Data data){
        AjaxResult ajaxResult = new AjaxResult();
        try {
            List<Integer> ids = data.getIds();
//            Iterator<Integer> iterator = ids.iterator();//
//            while (iterator.hasNext()){
//                //判断是否存在课程关联学生
//                if(!selectedCourseService.isStudentId(iterator.next())){
//                    ajaxResult.setSuccess(false);
//                    ajaxResult.setMessage("无法删除,存在课程关联学生");
//                    return ajaxResult;
//                }
//            }
            for (Integer id : ids) {
                //判断是否存在课程关联学生，选过课的同学无法删除
                if(!selectedCourseService.isStudentId(id)){
                    ajaxResult.setSuccess(false);
                    ajaxResult.setMessage("无法删除,存在课程关联学生");
                    return ajaxResult;
                }
            }
            //先删除图片
            File fileDir = UploadUtil.getImgDirFile();//获取上传图片的文件夹路径,如果没有就创建一个文件夹并返回该文件夹的路径
            for(Integer id : ids){
                Student stu = studentService.findById(id);
                if(!stu.getPhoto().isEmpty()){
                    //File.separator 的作用相当于 '\'
                    File file = new File(fileDir.getAbsolutePath() + File.separator + stu.getPhoto());
                    file.delete();
                }
            }
            //再删除数据库
            int count = studentService.deleteStudent(ids);
            if(count > 0){
                ajaxResult.setSuccess(true);
                ajaxResult.setMessage("全部删除成功");

            }else{
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("删除失败");
            }

        }catch (Exception e){
            e.printStackTrace();
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("删除失败");
        }
        return ajaxResult;
    }


    /**
     * 添加学生
     * @param files
     * @param student
     * @return
     * @throws IOException
     */
    @RequestMapping("/addStudent")
    @ResponseBody
    //MultipartFile是spring类型，代表HTML中以FromData方式上传的文件，包含二进制数据+文件名称
    public AjaxResult addStudent(@RequestParam("file") MultipartFile[] files,Student student) throws IOException {

        AjaxResult ajaxResult = new AjaxResult();
        student.setSn(SnGenerateUtil.generateSn(student.getClazzId()));

        // 存放上传图片的文件夹
        File fileDir = UploadUtil.getImgDirFile();
        //保存图片
        for(MultipartFile fileImg : files){

            // 拿到文件名
            //getOriginalFileName方法获取的是文件的完整名称，包括文件名称+文件拓展名
            //获取扩展名(.png)
            String extName = fileImg.getOriginalFilename().substring(fileImg.getOriginalFilename().lastIndexOf("."));
//            System.out.println(fileImg.getOriginalFilename());
            System.out.println(fileImg.getOriginalFilename().split("\\.")[1]);
//            String extName = fileImg.getOriginalFilename().split(".")[1];
            String uuidName = UUID.randomUUID().toString();

            try {
                // 构建真实的文件路径
                File newFile = new File(fileDir.getAbsolutePath() + File.separator +uuidName+ extName);

                // 上传图片到 -》 “绝对路径”
                //transferTo方法用来将接收文件传输到给定目标路径
                fileImg.transferTo(newFile);

            } catch (IOException e) {
                e.printStackTrace();
            }
            student.setPhoto(uuidName+extName);
        }
        //保存学生信息到数据库
        try{
            int count = studentService.addStudent(student);
            if(count > 0){
                ajaxResult.setSuccess(true);
                ajaxResult.setMessage("保存成功");
            }else{
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("保存失败");
            }
        }catch (Exception e){
            e.printStackTrace();
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("保存失败");
        }

        ajaxResult.setSuccess(true);
        return ajaxResult;
    }

    /**
     * 修改学生信息
     * @param files
     * @param student
     * @return
     */
    @PostMapping("/editStudent")
    @ResponseBody
    public AjaxResult editStudent(@RequestParam("file") MultipartFile[] files,Student student){
        AjaxResult ajaxResult = new AjaxResult();

        // 存放上传图片的文件夹
        File fileDir = UploadUtil.getImgDirFile();
        for(MultipartFile fileImg : files){

            String name = fileImg.getOriginalFilename();
            if(name.equals("")){
                break;
            }

            // 拿到文件名后缀
            String extName = fileImg.getOriginalFilename().substring(fileImg.getOriginalFilename().lastIndexOf("."));
            String uuidName = UUID.randomUUID().toString();

            try {
                // 构建真实的文件路径
                File newFile = new File(fileDir.getAbsolutePath() + File.separator +uuidName+ extName);
                // 上传图片到 -》 “绝对路径”
                fileImg.transferTo(newFile);
                Student stu = studentService.findById(student.getId());
                File file = new File(fileDir.getAbsolutePath() + File.separator + stu.getPhoto());
                System.out.println(file);
                file.delete();

            } catch (IOException e) {
                e.printStackTrace();
            }
            student.setPhoto(uuidName+extName);
        }

        try{
            int count = studentService.editStudent(student);
            if(count > 0){
                ajaxResult.setSuccess(true);
                ajaxResult.setMessage("修改成功");
            }else{
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("修改失败");
            }
        }catch(Exception e){
            e.printStackTrace();
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("修改失败");
        }
        return ajaxResult;
    }
}
