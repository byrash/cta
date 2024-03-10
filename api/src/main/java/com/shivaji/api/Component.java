package com.shivaji.api;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Component {

  private String key;
  private String label;
  private String type;
  private Validate validate;

}

