package com.example.democloudstreamwebfux.guava;

import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SampleCamera {

    public static void print(Camera camera) {

        System.out.println(camera.snap(new Color(125, 125, 125)));

    }

    public static void main(String[] args) {
        // print(new Camera());
    
        // print(new Camera(Color::brighter));
        // print(new Camera(Color::darker));
    
        // print(new Camera(Color::brighter, Color::darker));
      }
    
}

class Camera {

    private Function<Color,Color> filter ;

    public Camera(Function<Color,Color>... filters) {
        filter = Stream.of(filters)
                            .reduce(Function.identity(), Function::andThen);
    }

    public Color snap(Color input) {

        return filter.apply(input);
    }
}

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(staticName = "of")
class Color {
    int r ;
    int g;
    int b ;

    public Color brighter(Color color) {

        return Color.builder()
                        .r(color.getR())
                        .g(color.getG())
                        .b(color.getB() - 10)
                        .build()
                        ;
    } 

    public Color darker(Color color) {

        return Color.builder()
                        .r(color.getR())
                        .g(color.getG())
                        .b(color.getB() + 10)
                        .build()
                        ;
    } 

}
