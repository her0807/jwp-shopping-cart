package woowacourse.shoppingcart.dto;

import woowacourse.shoppingcart.domain.Product;

public class ProductRequest {

    private String name;
    private int price;
    private String imageUrl;
    private String description;
    private int stock;

    public ProductRequest() {
    }

    public ProductRequest(String name, int price, String imageUrl, String description, int stock) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.stock = stock;
    }

    public static Product toProduct(ProductRequest request) {
        return new Product(null,
                request.getName(),
                request.getPrice(),
                request.getImageUrl(),
                request.getDescription(),
                request.getStock());
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public int getStock() {
        return stock;
    }

    @Override
    public String toString() {
        return "ProductRequest{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", description='" + description + '\'' +
                ", stock=" + stock +
                '}';
    }
}
