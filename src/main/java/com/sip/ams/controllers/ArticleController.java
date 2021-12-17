package com.sip.ams.controllers;

import java.util.List;
import java.util.Random;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


import com.sip.ams.entities.Article;
import com.sip.ams.entities.Provider;
import com.sip.ams.repositories.ArticleRepository;
import com.sip.ams.repositories.ProviderRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/article/")
public class ArticleController {
	public static String uploadDirectory = System.getProperty("user.dir")+"/src/main/resources/static/uploads";
	private final ArticleRepository articleRepository;
	private final ProviderRepository providerRepository;
    @Autowired
    public ArticleController(ArticleRepository articleRepository, ProviderRepository providerRepository) {
        this.articleRepository = articleRepository;
        this.providerRepository = providerRepository;
    }
    
    @GetMapping("list")
    public String listArticles(Model model) {
    	//model.addAttribute("articles", null);
       
        List<Article> la =  (List<Article>)articleRepository.findAll();
    	if(la.size()==0)
    		la = null;
        model.addAttribute("articles",la);
        return "article/listArticles";
    }
    
    @GetMapping("add")
    public String showAddArticleForm(Article article, Model model) {
    	
    	model.addAttribute("providers",providerRepository.findAll());
    	model.addAttribute("article", new Article());
        return "article/addArticle";
    }
    
    @PostMapping("add")
    //@ResponseBody
    public String addArticle(@Valid Article article, BindingResult result, @RequestParam(name = "providerId", required = false) Long p,
    		@RequestParam("files") MultipartFile[] files
) {
    	if(p!=null)
    	{
    		Provider provider = providerRepository.findById(p)
                .orElseThrow(()-> new IllegalArgumentException("Invalid provider Id:" + p));
    	article.setProvider(provider);
    	}
    	
    	
    	/// start upload
    	
    	StringBuilder fileName = new StringBuilder();
    	LocalDateTime ldt = LocalDateTime.now();
    	MultipartFile file = files[0];
    	String finalName = getSaltString().concat(file.getOriginalFilename()); 
    	Path fileNameAndPath = Paths.get(uploadDirectory, finalName);
    	
    	fileName.append(finalName);
		  try {
			Files.write(fileNameAndPath, file.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		/// end upload
		 article.setPicture(fileName.toString());

    	 articleRepository.save(article);
    	 return "redirect:list";
    	
    	//return article.getLabel() + " " +article.getPrice() + " " + p.toString();
    }
    
    @GetMapping("delete/{id}")
    public String deleteProvider(@PathVariable("id") long id, Model model) {
        Article artice = articleRepository.findById(id)
            .orElseThrow(()-> new IllegalArgumentException("Invalid provider Id:" + id));
        articleRepository.delete(artice);
       // model.addAttribute("articles", articleRepository.findAll());
       // return "article/listArticles";
        return "redirect:../list";
    }
    
    @GetMapping("edit/{id}")
    public String showArticleFormToUpdate(@PathVariable("id") long id, Model model) {
    	Article article = articleRepository.findById(id)
            .orElseThrow(()->new IllegalArgumentException("Invalid provider Id:" + id));
    	
        model.addAttribute("article", article);
        model.addAttribute("providers", providerRepository.findAll());
        model.addAttribute("idProvider", article.getProvider().getId());
        
        return "article/updateArticle";
    }
    @PostMapping("edit/{id}")
    public String updateArticle(@PathVariable("id") long id, @Valid Article article, BindingResult result,
        Model model, @RequestParam(name = "providerId", required = false) Long p) {
        if (result.hasErrors()) {
        	article.setId(id);
            return "article/updateArticle";
        }
        
        Provider provider = providerRepository.findById(p)
                .orElseThrow(()-> new IllegalArgumentException("Invalid provider Id:" + p));
    	article.setProvider(provider);
    	
        articleRepository.save(article);
        model.addAttribute("articles", articleRepository.findAll());
        return "article/listArticles";
    }

 // rundom string to be used to the image name
 	protected static String getSaltString() {
 		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
 		StringBuilder salt = new StringBuilder();  
 		Random rnd = new Random();
 		while (salt.length() < 18) { // length of the random string.
 			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
 			salt.append(SALTCHARS.charAt(index));
 		}
 		String saltStr = salt.toString();
 		return saltStr;

 	}
 	
 	@GetMapping("show/{id}")
    public String showArticleDetails(@PathVariable("id") long id, Model model) {
    	Article article = articleRepository.findById(id)
            .orElseThrow(()->new IllegalArgumentException("Invalid provider Id:" + id));
    	
        model.addAttribute("article", article);
        
        return "article/showArticle";
    }

}
