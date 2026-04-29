public class Electronics extends Item {
    private String brand;    //thương hiệu
    private int warrantyMonths;       //bảo hành

    public Electronics(int itemId, int sellerId, String itemName, String description,
                       BigDecimal startPrice, String imageUrl, String brand, int warrantyMonths) {
        super(itemId, sellerId, itemName, description, "Electronics", startPrice, imageUrl);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public void printInfo() {
        System.out.println("Electronic Item: " + itemName + " | Brand: " + brand);
    }
}