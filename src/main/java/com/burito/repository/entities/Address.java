package com.burito.repository.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
public class Address {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String street;

  private String city;

  private String state;

  private String country;
  private String zipcode;

  public Address(Long id, String street, String city, String state,
                 String country,
                 String zipcode) {
    this.id = id;
    this.street = street;
    this.city = city;
    this.state = state;
    this.country = country;
    this.zipcode = zipcode;
  }
}