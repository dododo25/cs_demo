package com.cs.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Table
@Entity
public class UserData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String mail;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column
    private String address;

    @Column
    private String tel;

    public UserData() {}

    private UserData(Builder builder) {
        this.id = builder.id;
        this.mail = builder.mail;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.birthDate = builder.birthDate;
        this.address = builder.address;
        this.tel = builder.tel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMail() {
        return mail;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getAddress() {
        return address;
    }

    public String getTel() {
        return tel;
    }

    public static class Builder {

        private Long id;

        private String mail;

        private String firstName;

        private String lastName;

        private LocalDate birthDate;

        private String address;

        private String tel;

        private Builder() {}

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setMail(String mail) {
            this.mail = mail;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setBirthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public Builder setAddress(String address) {
            this.address = address;
            return this;
        }

        public Builder setTel(String tel) {
            this.tel = tel;
            return this;
        }

        public UserData build() {
            return new UserData(this);
        }

        public static Builder fromScratch() {
            return new Builder();
        }

        public static Builder basedAt(UserData data) {
            return new Builder()
                    .setId(data.id)
                    .setMail(data.mail)
                    .setFirstName(data.firstName)
                    .setLastName(data.lastName)
                    .setBirthDate(data.birthDate)
                    .setAddress(data.address)
                    .setTel(data.tel);
        }
    }
}
