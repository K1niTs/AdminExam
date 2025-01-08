db = connect("mongodb://localhost:27017/reviewsdb");

db.createCollection("reviews");

db.reviews.insertMany([
    { client: "John Doe", rating: 5, comment: "Great product!" },
    { client: "Jane Smith", rating: 4, comment: "Good service, but room for improvement." },
    { client: "Alice Brown", rating: 3, comment: "Average experience." }
]);
