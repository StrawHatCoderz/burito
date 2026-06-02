package com.burito.domain;

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
  private Long addressId;

  @Column(nullable = false)
  private String street;

  @Column(nullable = false)
  private String city;

  @Column(nullable = false)
  private String state;

  @Column(nullable = false)
  private String country;

  @Column(nullable = false)
  private String zipcode;

  public Address(Long addressId, String street, String city, String state,
                 String country,
                 String zipcode) {
    this.addressId = addressId;
    this.street = street;
    this.city = city;
    this.state = state;
    this.country = country;
    this.zipcode = zipcode;
  }
}