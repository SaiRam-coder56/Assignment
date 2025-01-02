const fs = require("fs");

// Function to parse the JSON file and calculate the constant term
function calculateSecret(filePath) {
  // Read and parse the JSON file
  const jsonData = JSON.parse(fs.readFileSync(filePath, "utf-8"));

  const { n, k } = jsonData.keys; // Extract n and k
  const points = [];

  // Parse the roots
  for (const key in jsonData) {
    if (key === "keys") continue;

    const x = parseInt(key); // X-coordinate
    const { base, value } = jsonData[key];
    const y = parseInt(value, base); // Decode Y-coordinate using the base

    points.push({ x, y });
  }

  // Sort the points by x (optional, ensures consistent processing)
  points.sort((a, b) => a.x - b.x);

  // Use Lagrange Interpolation to calculate the constant term
  const constant = lagrangeInterpolation(points.slice(0, k), 0); // Evaluate at x = 0
  console.log("The secret (constant term c) is:", constant);
}

// Function to calculate Lagrange Interpolation at a given x
function lagrangeInterpolation(points, x) {
  let result = 0;

  for (let i = 0; i < points.length; i++) {
    const { x: xi, y: yi } = points[i];
    let term = yi;

    for (let j = 0; j < points.length; j++) {
      if (i !== j) {
        const { x: xj } = points[j];
        term *= (x - xj) / (xi - xj);
      }
    }

    result += term;
  }

  return Math.round(result); // Round to handle floating-point precision
}

// Run the function with your input file
const filePath = "input2.json"; // Ensure your JSON file is in the same directory
calculateSecret(filePath);

