package com.ner3k.cinephilia.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.NaturalId;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @Pattern(regexp = "^[a-zA-Z]+$", message = "User Name Contains invalid characters")
    @NotEmpty(message = "User Name is required!")
    @Size(message = "Size must be between 4 and 30 Characters!")
    private String username;


    @NotEmpty(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;


    @NotEmpty(message = "Password must not be empty")
    @Size(min = 8, max = 128, message = "Password must be at least 8 and at most 128 characters")
    private String password;


    @Transient
    private String confirmPassword;

    @NotNull(message = "BOD must not be null")
    @ValidAge(minimumAge = 12, message = "You must be at least 12 years old")
    private LocalDate dob;

    public boolean isAdult() {
        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(dob, currentDate);
        return period.getYears() >= 18;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_wishes",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id"))
    private List<Movie> wishes;


    @OneToMany(mappedBy="user", fetch=FetchType.LAZY)
    private List<Review> reviews;



    @OneToMany( mappedBy = "user",fetch = FetchType.LAZY)
    List<Rate> rates;

    boolean dark = true;
    public List<Long> getRatedMoviesId(){
        List<Rate> rates = getRates();
        List<Long> movieIds = new ArrayList<>();
        for(Rate rate : rates){
            movieIds.add(rate.getMovie().getId());
        }
        return movieIds;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @Column(updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDate.now();
    }
}
