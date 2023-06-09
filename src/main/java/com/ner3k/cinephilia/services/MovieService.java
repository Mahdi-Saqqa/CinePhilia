package com.ner3k.cinephilia.services;


import com.ner3k.cinephilia.models.*;
import com.ner3k.cinephilia.repositories.GenreRepository;
import com.ner3k.cinephilia.repositories.MovieRepository;
import com.ner3k.cinephilia.repositories.RateRepository;
import java.io.IOException;
import java.util.*;

import com.ner3k.cinephilia.repositories.ReviewRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MovieService {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private RateRepository rateRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    public List<Movie> getMoviesByGenre(Long id) throws ParseException {
        Genre genre = genreRepository.getById(id);
        return genre.getMovies();
    }
    public Genre getGenreByID(Long id) throws ParseException {

        return genreRepository.getById(id);
    }
    public void deleteReview(Long id) throws ParseException {
        reviewRepository.deleteById(id);
    }
    public Review getReviewByID(Long id) throws ParseException {
        return reviewRepository.getById(id);
    }
    public String getMovieCertificate(String id) {
        final String uri = "https://api.themoviedb.org/3/movie/" + id + "/videos?api_key=936ec287a61d8548efeb41653e17f492";

        RestTemplate restTemplate = new RestTemplate();
        HashMap response=restTemplate.getForObject(uri, HashMap.class);

        List <HashMap>  trailers = (List<HashMap>) response.get("results");
        String url = null;
        for(HashMap trailer : trailers) {
            url = (String) trailer.get("key");
        }
        System.out.println(url);
        return url;
    }


    public String getMovieTrailer(String id) {
        final String uri = "https://api.themoviedb.org/3/movie/" + id + "/videos?api_key=936ec287a61d8548efeb41653e17f492";

        RestTemplate restTemplate = new RestTemplate();
        HashMap response=restTemplate.getForObject(uri, HashMap.class);

        List <HashMap>  trailers = (List<HashMap>) response.get("results");
        String url = null;
        for(HashMap trailer : trailers) {
            url = (String) trailer.get("key");
        }
        System.out.println(url);
        return url;
    }
    public static HashMap getMovie(String id) throws ParseException {
        final String uri = "https://api.themoviedb.org/3/movie/" + id + "?api_key=936ec287a61d8548efeb41653e17f492";

        RestTemplate restTemplate = new RestTemplate();



        return restTemplate.getForObject(uri, HashMap.class);
    }

    public Movie addMovie(String id ,boolean isAdult) throws ParseException, IOException {

        HashMap result = getMovie(id);
        System.out.println(result);
        Movie movie = new Movie();
        movie.setTitle((String) result.get("title"));
        movie.setTrailer(getMovieTrailer(id));
        movie.setTmdbId(id);
        System.out.println(movie.getTitle());
        movie.setAdult(isAdult);
        movie.setOverview((String) result.get("overview"));
        movie.setPoster((String) result.get("poster_path"));
        movie.setLanguage((String) result.get("original_language"));
        List<Genre> addedGenres = new ArrayList<>();
        List <HashMap>  genres = (List<HashMap>) result.get("genres");
        for(HashMap genre : genres){
            String name = String.valueOf(genre.get("name"));
            if(genreRepository.existsByName(name)){
                addedGenres.add(genreRepository.findByName(name));
            }
            else{
                Genre newGenre = new Genre();
                newGenre.setName(name);
                addedGenres.add(genreRepository.save(newGenre));
            }
            movie.setGenres(addedGenres);
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/certification/movie/list")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5MzZlYzI4N2E2MWQ4NTQ4ZWZlYjQxNjUzZTE3ZjQ5MiIsInN1YiI6IjY0NjMzMjQ1MGYzNjU1MDBmY2RmYzQxNCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.F1PAFG_j2NacHIxqZUt6Gj_d59AcrJLPxTmOE66G8e8")
                .build();
        Response response = client.newCall(request).execute();
        assert response.body() != null;



        return movieRepository.save(movie);
    }

    public Review addReviewToMovie( Movie movie,User user,String reviewbody){
        Review review = new Review();

        review.setReview(reviewbody);
       review.setUser(user);
       review.setMovie(movie);
       return reviewRepository.save(review);
    }
    public List<Movie> getAllMovies() {

        return movieRepository.findAllByOrderByIdDesc();
    }
    public  Movie getMovie(Long id) {

        if(movieRepository.findById(id).isPresent()){
            return movieRepository.findById(id).get();
        }
        else return null;
    }


    public void rateMovie(Movie movie, int rate,User user) {
        Rate userrate = new Rate();
        userrate.setRate(rate);
        userrate.setMovie(movie);
        userrate.setUser(user);
        rateRepository.save(userrate);
    }
    public void updateMovieRate(Rate userrate){
        rateRepository.save(userrate);
    }

    public List<Movie> getRandomMovies() {
        List<Movie> randMovies = new ArrayList<Movie>();

        for(int i = 0; i <20;i++){
            List<Movie> movies = movieRepository.findAll();
            Random random = new Random();
            int randomIndex = random.nextInt(movies.size());
            Movie movie = movies.get(randomIndex);
            if (movie != null){
                randMovies.add(movie);
            }
            else {
                i--;
            }
        }
        return randMovies;
    }


    public void updateReview(Review review1, String review) {
        review1.setReview(review);
        reviewRepository.save(review1);
    }

    public Movie getMovieById(Long id) {
        if(movieRepository.findById(id).isPresent()) {
            return movieRepository.findById(id).get();
        }
        else return null;
    }

    public void deleteMovie(Movie movie) {
        movieRepository.delete(movie);
    }

    public void updateMovie(Movie movie) {
        movieRepository.save(movie);
    }

    public List<Movie> filter(List<Movie> allMovies) {
        allMovies.removeIf(Movie::isAdult);
        return allMovies;
    }
}
