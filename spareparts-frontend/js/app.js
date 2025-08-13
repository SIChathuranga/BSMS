import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import { getAuth, GoogleAuthProvider, signInWithPopup, onAuthStateChanged, signOut, getIdToken } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";
import { addToCart, getCart, checkout, clearCart } from './cart.js';

// Firebase config
const firebaseConfig = {
  apiKey: "AIzaSyCeQB04PkgVJ7lXveCMF0g83uIJtZr49wA",
  authDomain: "bsms-project.firebaseapp.com",
  projectId: "bsms-project",
  storageBucket: "bsms-project.appspot.com",
  messagingSenderId: "889312176803",
  appId: "1:889312176803:web:a1aa50b15058ffd7f66d16",
  measurementId: "G-L740JC7MME"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const provider = new GoogleAuthProvider();

// DOM Elements
const loginBtn = document.getElementById('loginBtn');
const logoutBtn = document.getElementById('logoutBtn');
const userProfile = document.getElementById('userProfile');
const userAvatar = document.getElementById('userAvatar');
const userName = document.getElementById('userName');
const productGrid = document.getElementById('productGrid');
const cartCount = document.getElementById('cartCount');
const checkoutBtn = document.getElementById('checkoutBtn');
const clearCartBtn = document.getElementById('clearCartBtn');
const loadingState = document.getElementById('loadingState');
const noResults = document.getElementById('noResults');
const productCountBadge = document.getElementById('productCount');
const searchInput = document.getElementById('search');
const mobileSearchInput = document.getElementById('mobileSearch');
const categoryFilter = document.getElementById('categoryFilter');
const categoryDropdown = document.getElementById('categoryDropdown');

// Global variables
let allProducts = [];
let filteredProducts = [];
let currentCategory = 'all';
let currentSearchTerm = '';

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
  initializeEventListeners();
  fetchProducts();
  refreshCartCount();
});

// Event Listeners
function initializeEventListeners() {
  // Authentication
  if (loginBtn) {
    loginBtn.addEventListener('click', handleLogin);
  }
  if (logoutBtn) {
    logoutBtn.addEventListener('click', handleLogout);
  }

  // Search functionality
  if (searchInput) {
    searchInput.addEventListener('input', debounce(handleSearch, 300));
  }
  if (mobileSearchInput) {
    mobileSearchInput.addEventListener('input', debounce(handleSearch, 300));
  }

  // Category filter
  if (categoryFilter) {
    categoryFilter.addEventListener('click', handleCategoryFilter);
  }
  
  // Category dropdown
  if (categoryDropdown) {
    categoryDropdown.addEventListener('change', handleCategoryDropdown);
  }

  // Cart actions
  if (checkoutBtn) {
    checkoutBtn.addEventListener('click', handleCheckout);
  }
  if (clearCartBtn) {
    clearCartBtn.addEventListener('click', handleClearCart);
  }

  // Auth state listener
  onAuthStateChanged(auth, handleAuthStateChange);
}

// Authentication handlers
async function handleLogin() {
  try {
    showToast('Signing in...', 'info');
    const result = await signInWithPopup(auth, provider);
    showToast('Login successful!', 'success');
    console.log('Login successful:', result.user);
  } catch (error) {
    console.error('Login failed:', error);
    showToast('Login failed: ' + error.message, 'error');
  }
}

async function handleLogout() {
  try {
    await signOut(auth);
    showToast('Logged out successfully', 'success');
    console.log('Logout successful');
  } catch (error) {
    console.error('Logout failed:', error);
    showToast('Logout failed', 'error');
  }
}

function handleAuthStateChange(user) {
  if (user) {
    // User is signed in
    loginBtn.classList.add('d-none');
    userProfile.classList.remove('d-none');
    userProfile.classList.add('d-flex');
    
    if (userAvatar && userName) {
      userAvatar.src = user.photoURL || 'https://via.placeholder.com/32x32?text=U';
      userName.textContent = user.displayName || user.email;
    }
  } else {
    // User is signed out
    loginBtn.classList.remove('d-none');
    userProfile.classList.add('d-none');
    userProfile.classList.remove('d-flex');
  }
}

// Product fetching and rendering
async function fetchProducts() {
  try {
    showLoading(true);
    const response = await fetch('http://localhost:8081/api/public/products');
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const products = await response.json();
    allProducts = products;
    filteredProducts = products;
    
    renderProducts(products);
    updateProductCount(products.length);
    showLoading(false);
    
  } catch (error) {
    console.error('Failed to fetch products:', error);
    showError('Failed to load products. Please try again later.');
    showLoading(false);
  }
}

function renderProducts(products) {
  if (!productGrid) return;

  if (products.length === 0) {
    showNoResults(true);
    return;
  }

  showNoResults(false);
  productGrid.classList.remove('d-none');
  
  productGrid.innerHTML = products.map(product => createProductCard(product)).join('');
  
  // Add event listeners to product cards
  attachProductEventListeners(products);
  
  // Add AOS animation to new cards
  if (typeof AOS !== 'undefined') {
    AOS.refresh();
  }
}

function createProductCard(product) {
  const stockClass = getStockClass(product.stock);
  const stockText = getStockText(product.stock);
  const imageUrl = (product.images && product.images[0]) || 'https://via.placeholder.com/400x300?text=Motorbike+Part';
  
  return `
    <div class="product-card" data-aos="fade-up" data-aos-delay="${Math.random() * 200}">
      <div class="stock-badge ${stockClass}">${stockText}</div>
      <a href="product.html?id=${product.id}">
        <img src="${imageUrl}" alt="${product.name}" loading="lazy">
      </a>
      <div class="card-body">
        <h5 class="card-title">
          <a href="product.html?id=${product.id}">${product.name}</a>
        </h5>
        <p class="card-text">${product.description || 'High-quality motorbike spare part'}</p>
        <div class="price">$${product.price}</div>
        <button class="btn btn-primary btn-sm w-100" 
                data-product-id="${product.id}" 
                ${product.stock <= 0 ? 'disabled' : ''}>
          <i class="bi bi-cart-plus me-1"></i>
          ${product.stock <= 0 ? 'Out of Stock' : 'Add to Cart'}
        </button>
      </div>
    </div>
  `;
}

function attachProductEventListeners(products) {
  const addToCartButtons = productGrid.querySelectorAll('button[data-product-id]');
  
  addToCartButtons.forEach(button => {
    button.addEventListener('click', function() {
      const productId = parseInt(this.getAttribute('data-product-id'));
      const product = products.find(p => p.id === productId);
      
      if (product && product.stock > 0) {
        addToCart(product, 1);
        refreshCartCount();
        showToast(`${product.name} added to cart!`, 'success');
        
        // Add visual feedback
        this.innerHTML = '<i class="bi bi-check2 me-1"></i>Added!';
        this.classList.add('btn-success');
        this.classList.remove('btn-primary');
        
        setTimeout(() => {
          this.innerHTML = '<i class="bi bi-cart-plus me-1"></i>Add to Cart';
          this.classList.add('btn-primary');
          this.classList.remove('btn-success');
        }, 1500);
      }
    });
  });
}

// Search functionality
function handleSearch(event) {
  currentSearchTerm = event.target.value.toLowerCase().trim();
  
  // Sync search inputs
  if (event.target.id === 'search' && mobileSearchInput) {
    mobileSearchInput.value = event.target.value;
  } else if (event.target.id === 'mobileSearch' && searchInput) {
    searchInput.value = event.target.value;
  }
  
  filterProducts();
}

// Category filtering
function handleCategoryFilter(event) {
  if (!event.target.hasAttribute('data-category')) return;
  
  const category = event.target.getAttribute('data-category');
  currentCategory = category;
  
  // Update active state
  categoryFilter.querySelectorAll('.btn').forEach(btn => {
    btn.classList.remove('active');
  });
  event.target.classList.add('active');
  
  // Sync dropdown if exists
  if (categoryDropdown) {
    categoryDropdown.value = category;
  }
  
  filterProducts();
}

// Category dropdown filtering
function handleCategoryDropdown(event) {
  const category = event.target.value;
  currentCategory = category;
  
  // Sync buttons if exists
  if (categoryFilter) {
    categoryFilter.querySelectorAll('.btn').forEach(btn => {
      btn.classList.remove('active');
    });
    const targetBtn = categoryFilter.querySelector(`[data-category="${category}"]`);
    if (targetBtn) {
      targetBtn.classList.add('active');
    }
  }
  
  filterProducts();
}

// Filter products based on search and category
function filterProducts() {
  let filtered = [...allProducts];
  
  // Apply category filter
  if (currentCategory !== 'all') {
    filtered = filtered.filter(product => 
      product.category?.toLowerCase().includes(currentCategory) ||
      product.name.toLowerCase().includes(currentCategory) ||
      product.description?.toLowerCase().includes(currentCategory)
    );
  }
  
  // Apply search filter
  if (currentSearchTerm) {
    filtered = filtered.filter(product =>
      product.name.toLowerCase().includes(currentSearchTerm) ||
      product.description?.toLowerCase().includes(currentSearchTerm) ||
      product.category?.toLowerCase().includes(currentSearchTerm)
    );
  }
  
  filteredProducts = filtered;
  renderProducts(filtered);
  updateProductCount(filtered.length);
}

// Cart functionality
function refreshCartCount() {
  const cart = getCart();
  const totalItems = cart.reduce((total, item) => total + item.quantity, 0);
  
  if (cartCount) {
    cartCount.textContent = totalItems;
    cartCount.style.display = totalItems > 0 ? 'flex' : 'none';
  }
}

async function handleCheckout() {
  try {
    const cart = getCart();
    if (cart.length === 0) {
      showToast('Your cart is empty', 'warning');
      return;
    }
    
    showToast('Processing your order...', 'info');
    const order = await checkout();
    refreshCartCount();
    showToast(`Order placed successfully! Order #${order.id}`, 'success');
    
  } catch (error) {
    console.error('Checkout failed:', error);
    showToast('Checkout failed. Please try again.', 'error');
  }
}

function handleClearCart() {
  const cart = getCart();
  if (cart.length === 0) {
    showToast('Your cart is already empty', 'info');
    return;
  }
  
  if (confirm('Are you sure you want to clear your cart?')) {
    clearCart();
    refreshCartCount();
    showToast('Cart cleared successfully', 'success');
  }
}

// Utility functions
function getStockClass(stock) {
  if (stock > 10) return 'in-stock';
  if (stock > 0) return 'low-stock';
  return 'out-of-stock';
}

function getStockText(stock) {
  if (stock > 10) return 'In Stock';
  if (stock > 0) return 'Low Stock';
  return 'Out of Stock';
}

function updateProductCount(count) {
  if (productCountBadge) {
    productCountBadge.textContent = `${count} Products`;
  }
}

function showLoading(show) {
  if (loadingState) {
    loadingState.style.display = show ? 'block' : 'none';
  }
  if (productGrid) {
    productGrid.style.display = show ? 'none' : 'grid';
  }
}

function showNoResults(show) {
  if (noResults) {
    noResults.classList.toggle('d-none', !show);
  }
  if (productGrid) {
    productGrid.classList.toggle('d-none', show);
  }
}

function showError(message) {
  showToast(message, 'error');
  showNoResults(true);
}

// Toast notification system
function showToast(message, type = 'info') {
  // Create toast container if it doesn't exist
  let toastContainer = document.getElementById('toast-container');
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.id = 'toast-container';
    toastContainer.className = 'position-fixed top-0 end-0 p-3';
    toastContainer.style.zIndex = '9999';
    document.body.appendChild(toastContainer);
  }
  
  const toastId = `toast-${Date.now()}`;
  const iconMap = {
    success: 'bi-check-circle-fill',
    error: 'bi-exclamation-triangle-fill',
    warning: 'bi-exclamation-triangle-fill',
    info: 'bi-info-circle-fill'
  };
  
  const bgMap = {
    success: 'bg-success',
    error: 'bg-danger',
    warning: 'bg-warning',
    info: 'bg-primary'
  };
  
  const toast = document.createElement('div');
  toast.id = toastId;
  toast.className = `toast align-items-center text-white ${bgMap[type]} border-0`;
  toast.setAttribute('role', 'alert');
  toast.innerHTML = `
    <div class="d-flex">
      <div class="toast-body">
        <i class="bi ${iconMap[type]} me-2"></i>
        ${message}
      </div>
      <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
    </div>
  `;
  
  toastContainer.appendChild(toast);
  
  const bsToast = new bootstrap.Toast(toast, {
    autohide: true,
    delay: type === 'error' ? 5000 : 3000
  });
  
  bsToast.show();
  
  // Remove toast element after it's hidden
  toast.addEventListener('hidden.bs.toast', () => {
    toast.remove();
  });
}

// Debounce function for search
function debounce(func, wait) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

// Reset filters function (called from HTML)
window.resetFilters = function() {
  currentCategory = 'all';
  currentSearchTerm = '';
  
  // Reset UI
  if (searchInput) searchInput.value = '';
  if (mobileSearchInput) mobileSearchInput.value = '';
  
  const activeBtn = categoryFilter?.querySelector('.btn.active');
  if (activeBtn) activeBtn.classList.remove('active');
  
  const allBtn = categoryFilter?.querySelector('[data-category="all"]');
  if (allBtn) allBtn.classList.add('active');
  
  // Re-filter products
  filterProducts();
};

// Export functions for external use
export async function authorizedFetch(url, options = {}) {
  const user = auth.currentUser;
  if (user) {
    const token = await getIdToken(user, true);
    options.headers = Object.assign({}, options.headers, { 
      'Authorization': `Bearer ${token}` 
    });
  }
  return fetch(url, options);
}

export async function adminFetch(url, options = {}) {
  const credentials = btoa('admin:admin123');
  options.headers = Object.assign({}, options.headers, { 
    'Authorization': `Basic ${credentials}` 
  });
  return fetch(url, options);
}
