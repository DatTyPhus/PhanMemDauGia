public class Vehicle extends Item {
    private String brand; //thương hiệu

    public Vehicle(int itemId, int sellerId, String itemName, String description,
                   BigDecimal startPrice, String imageUrl,String model) {
        super(itemId, sellerId, itemName, description, "Vehicle", startPrice, imageUrl);
        this.model = model;
    }

    @Override
    public void printInfo() {
        System.out.println("Vehicle: " + itemName + " | Brand: " + brand);
    }
}