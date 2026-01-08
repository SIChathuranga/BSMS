/**
 * BSMS - Motorbike Spare Parts Management System
 * Main Application JavaScript v2.0
 */

import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import { 
  getAuth, 
  GoogleAuthProvider, 
  signInWithPopup, 
  onAuthStateChanged, 
  signOut, 
  getIdToken 
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";

// ============== CONFIGURATION ==============
const API_BASE_URL = 'http://localhost:8080/api/v2';
const LEGACY_API_URL = 'http://localhost:8080/api'; // Fallback

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

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const provider = new GoogleAuthProvider();

// ============== STATE ==============
let allProducts = [];
let filteredProducts = [];
let currentCategory = 'all';
let currentSearchTerm = '';
let cart = JSON.parse(localStorage.getItem('bsms_cart') || '[]');
let currentUser = null;

// ============== DOM ELEMENTS ==============
const elements = {
  loginBtn: document.getElementById('loginBtn'),
  logoutBtn: document.getElementById('logoutBtn'),
  userProfile: document.getElementById('userProfile'),
  userAvatar: document.getElementById('userAvatar'),
  userName: document.getElementById('userName'),
  productGrid: document.getElementById('productGrid'),
  cartCount: document.getElementById('cartCount'),
  checkoutBtn: document.getElementById('checkoutBtn'),
  clearCartBtn: document.getElementById('clearCartBtn'),
  loadingState: document.getElementById('loadingState'),
  noResults: document.getElementById('noResults'),
  productCountBadge: document.getElementById('productCount'),
  headerSearch: document.getElementById('headerSearch'),
  mobileSearch: document.getElementById('mobileSearch'),
  categoryFilter: document.getElementById('categoryFilter'),
  viewCartBtn: document.getElementById('viewCartBtn'),
  modalCheckoutBtn: document.getElementById('modalCheckoutBtn'),
  cartModalBody: document.getElementById('cartModalBody'),
  cartTotal: document.getElementById('cartTotal'),
  checkoutForm: document.getElementById('checkoutForm')
};

// ============== INITIALIZATION ==============
document.addEventListener('DOMContentLoaded', function() {
  initializeEventListeners();
  fetchProducts();
  updateCartUI();
  
  // Auth state listener
  onAuthStateChanged(auth, handleAuthStateChange);
});

// ============== EVENT LISTENERS ==============
function initializeEventListeners() {
  // Authentication
  if (elements.loginBtn) {
    elements.loginBtn.addEventListener('click', handleLogin);
  }
  if (elements.logoutBtn) {
    elements.logoutBtn.addEventListener('click', handleLogout);
  }

  // Search functionality
  if (elements.headerSearch) {
    elements.headerSearch.addEventListener('input', debounce(handleSearch, 300));
  }
  if (elements.mobileSearch) {
    elements.mobileSearch.addEventListener('input', debounce(handleSearch, 300));
  }

  // Category filter
  if (elements.categoryFilter) {
    elements.categoryFilter.addEventListener('click', handleCategoryFilter);
  }

  // Cart actions
  if (elements.checkoutBtn) {
    elements.checkoutBtn.addEventListener('click', handleCheckout);
  }
  if (elements.clearCartBtn) {
    elements.clearCartBtn.addEventListener('click', handleClearCart);
  }
  if (elements.viewCartBtn) {
    elements.viewCartBtn.addEventListener('click', showCartModal);
  }
  if (elements.modalCheckoutBtn) {
    elements.modalCheckoutBtn.addEventListener('click', showCheckoutModal);
  }
  
  // Checkout form
  if (elements.checkoutForm) {
    elements.checkoutForm.addEventListener('submit', handleCheckoutSubmit);
  }
}

// ============== AUTHENTICATION ==============
async function handleLogin() {
  try {
    showToast('Signing in...', 'info');
    const result = await signInWithPopup(auth, provider);
    
    // Sync user profile with backend
    await syncUserProfile(result.user);
    
    showToast('Login successful!', 'success');
  } catch (error) {
    console.error('Login failed:', error);
    showToast('Login failed: ' + error.message, 'error');
  }
}

async function handleLogout() {
  try {
    await signOut(auth);
    currentUser = null;
    showToast('Logged out successfully', 'success');
  } catch (error) {
    console.error('Logout failed:', error);
    showToast('Logout failed', 'error');
  }
}

function handleAuthStateChange(user) {
  currentUser = user;
  
  if (user) {
    elements.loginBtn?.classList.add('d-none');
    elements.userProfile?.classList.remove('d-none');
    elements.userProfile?.classList.add('d-flex');
    
    if (elements.userAvatar && elements.userName) {
      elements.userAvatar.src = user.photoURL || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(user.displayName || 'U');
      elements.userName.textContent = user.displayName?.split(' ')[0] || user.email?.split('@')[0];
    }
  } else {
    elements.loginBtn?.classList.remove('d-none');
    elements.userProfile?.classList.add('d-none');
    elements.userProfile?.classList.remove('d-flex');
  }
}

async function syncUserProfile(user) {
  try {
    const token = await getIdToken(user, true);
    await fetch(`${API_BASE_URL}/user/profile/sync`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        uid: user.uid,
        email: user.email,
        displayName: user.displayName,
        photoUrl: user.photoURL
      })
    });
  } catch (error) {
    console.warn('Profile sync failed:', error);
  }
}

// ============== PRODUCTS ==============
async function fetchProducts() {
  try {
    showLoading(true);
    
    // Try v2 API first, fallback to legacy
    let response = await fetch(`${API_BASE_URL}/public/products`);
    
    if (!response.ok) {
      response = await fetch(`${LEGACY_API_URL}/public/products`);
    }
    
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
  if (!elements.productGrid) return;

  if (products.length === 0) {
    showNoResults(true);
    return;
  }

  showNoResults(false);
  elements.productGrid.classList.remove('d-none');
  
  elements.productGrid.innerHTML = products.map((product, index) => 
    createProductCard(product, index)
  ).join('');
  
  // Attach event listeners
  attachProductEventListeners(products);
  
  // Refresh AOS
  if (typeof AOS !== 'undefined') {
    AOS.refresh();
  }
}

function createProductCard(product, index) {
  const stockClass = getStockBadgeClass(product.stock);
  const stockText = getStockText(product.stock);
  const imageUrl = (product.images && product.images[0]) || 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400';
  const isInCart = cart.some(item => item.id === product.id);
  const delay = Math.min(index * 50, 300);
  
  // Handle both string IDs (Firestore) and numeric IDs (legacy)
  const productId = product.id;
  const categoryDisplay = product.category ? product.category.charAt(0).toUpperCase() + product.category.slice(1) : 'Parts';
  
  return `
    <div class="product-card" data-aos="fade-up" data-aos-delay="${delay}">
      <div class="product-card-image">
        <a href="product.html?id=${productId}">
          <img src="${imageUrl}" alt="${product.name}" loading="lazy">
        </a>
        <div class="product-card-badges">
          <span class="badge ${stockClass}">${stockText}</span>
        </div>
        <button class="product-card-wishlist ${isWishlisted(productId) ? 'active' : ''}" 
                data-product-id="${productId}" 
                title="${isWishlisted(productId) ? 'Remove from wishlist' : 'Add to wishlist'}">
          <i class="bi ${isWishlisted(productId) ? 'bi-heart-fill' : 'bi-heart'}"></i>
        </button>
      </div>
      <div class="product-card-body">
        <span class="product-card-category">${categoryDisplay}</span>
        <h5 class="product-card-title">
          <a href="product.html?id=${productId}">${product.name}</a>
        </h5>
        <p class="product-card-description">${product.description || 'High-quality motorbike spare part'}</p>
        ${product.averageRating > 0 ? `
          <div class="product-card-meta">
            <div class="product-card-rating">
              <i class="bi bi-star-fill"></i>
              <span>${product.averageRating.toFixed(1)}</span>
            </div>
            <span class="product-card-reviews">(${product.reviewCount || 0} reviews)</span>
          </div>
        ` : ''}
        <div class="product-card-footer">
          <div class="product-card-price">$${formatPrice(product.price)}</div>
          <button class="btn btn-primary btn-sm add-to-cart-btn" 
                  data-product-id="${productId}" 
                  ${product.stock <= 0 ? 'disabled' : ''}>
            <i class="bi ${isInCart ? 'bi-check2' : 'bi-cart-plus'} me-1"></i>
            ${product.stock <= 0 ? 'Out of Stock' : isInCart ? 'Added' : 'Add'}
          </button>
        </div>
      </div>
    </div>
  `;
}

function attachProductEventListeners(products) {
  // Add to cart buttons
  document.querySelectorAll('.add-to-cart-btn').forEach(button => {
    button.addEventListener('click', function(e) {
      e.preventDefault();
      const productId = this.getAttribute('data-product-id');
      const product = products.find(p => String(p.id) === String(productId));
      
      if (product && product.stock > 0) {
        addToCart(product);
        
        // Update button UI
        this.innerHTML = '<i class="bi bi-check2 me-1"></i>Added!';
        this.classList.add('btn-success');
        this.classList.remove('btn-primary');
        
        setTimeout(() => {
          this.innerHTML = '<i class="bi bi-cart-plus me-1"></i>Add';
          this.classList.add('btn-primary');
          this.classList.remove('btn-success');
        }, 1500);
      }
    });
  });
  
  // Wishlist buttons
  document.querySelectorAll('.product-card-wishlist').forEach(button => {
    button.addEventListener('click', function(e) {
      e.preventDefault();
      const productId = this.getAttribute('data-product-id');
      toggleWishlist(productId, this);
    });
  });
}

// ============== SEARCH & FILTERING ==============
function handleSearch(event) {
  currentSearchTerm = event.target.value.toLowerCase().trim();
  
  // Sync search inputs
  if (event.target.id === 'headerSearch' && elements.mobileSearch) {
    elements.mobileSearch.value = event.target.value;
  } else if (event.target.id === 'mobileSearch' && elements.headerSearch) {
    elements.headerSearch.value = event.target.value;
  }
  
  filterProducts();
}

function handleCategoryFilter(event) {
  const target = event.target.closest('.category-btn');
  if (!target) return;
  
  const category = target.getAttribute('data-category');
  currentCategory = category;
  
  // Update active state
  document.querySelectorAll('.category-btn').forEach(btn => {
    btn.classList.remove('active');
  });
  target.classList.add('active');
  
  filterProducts();
}

function filterProducts() {
  let filtered = [...allProducts];
  
  // Apply category filter
  if (currentCategory !== 'all') {
    filtered = filtered.filter(product => 
      product.category?.toLowerCase() === currentCategory.toLowerCase() ||
      product.name?.toLowerCase().includes(currentCategory) ||
      product.description?.toLowerCase().includes(currentCategory)
    );
  }
  
  // Apply search filter
  if (currentSearchTerm) {
    filtered = filtered.filter(product =>
      product.name?.toLowerCase().includes(currentSearchTerm) ||
      product.description?.toLowerCase().includes(currentSearchTerm) ||
      product.category?.toLowerCase().includes(currentSearchTerm) ||
      product.brand?.toLowerCase().includes(currentSearchTerm) ||
      product.sku?.toLowerCase().includes(currentSearchTerm)
    );
  }
  
  filteredProducts = filtered;
  renderProducts(filtered);
  updateProductCount(filtered.length);
}

window.resetFilters = function() {
  currentCategory = 'all';
  currentSearchTerm = '';
  
  if (elements.headerSearch) elements.headerSearch.value = '';
  if (elements.mobileSearch) elements.mobileSearch.value = '';
  
  document.querySelectorAll('.category-btn').forEach(btn => {
    btn.classList.remove('active');
  });
  document.querySelector('[data-category="all"]')?.classList.add('active');
  
  filterProducts();
};

// ============== CART ==============
function addToCart(product, quantity = 1) {
  const existingItem = cart.find(item => String(item.id) === String(product.id));
  
  if (existingItem) {
    existingItem.quantity += quantity;
  } else {
    cart.push({
      id: product.id,
      name: product.name,
      price: product.price,
      image: product.images?.[0] || '',
      quantity: quantity
    });
  }
  
  saveCart();
  updateCartUI();
  showToast(`${product.name} added to cart!`, 'success');
}

function removeFromCart(productId) {
  cart = cart.filter(item => String(item.id) !== String(productId));
  saveCart();
  updateCartUI();
  renderCartModal();
}

function updateCartQuantity(productId, quantity) {
  const item = cart.find(item => String(item.id) === String(productId));
  if (item) {
    item.quantity = Math.max(1, quantity);
    saveCart();
    updateCartUI();
    renderCartModal();
  }
}

function saveCart() {
  localStorage.setItem('bsms_cart', JSON.stringify(cart));
}

function updateCartUI() {
  const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
  if (elements.cartCount) {
    elements.cartCount.textContent = totalItems;
    elements.cartCount.style.display = totalItems > 0 ? 'flex' : 'none';
  }
}

function getCartTotal() {
  return cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
}

function showCartModal() {
  renderCartModal();
  const modal = new bootstrap.Modal(document.getElementById('cartModal'));
  modal.show();
}

function renderCartModal() {
  if (!elements.cartModalBody) return;
  
  if (cart.length === 0) {
    elements.cartModalBody.innerHTML = `
      <div class="text-center py-5">
        <i class="bi bi-cart-x" style="font-size: 4rem; color: var(--text-muted);"></i>
        <h5 class="mt-3">Your cart is empty</h5>
        <p class="text-muted">Add some products to get started!</p>
      </div>
    `;
    if (elements.cartTotal) elements.cartTotal.textContent = '$0.00';
    return;
  }
  
  elements.cartModalBody.innerHTML = `
    <div class="table-responsive">
      <table class="table align-middle">
        <thead>
          <tr>
            <th>Product</th>
            <th>Price</th>
            <th>Qty</th>
            <th>Total</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          ${cart.map(item => `
            <tr>
              <td>
                <div class="d-flex align-items-center">
                  <img src="${item.image || 'https://via.placeholder.com/50'}" 
                       class="rounded me-3" width="50" height="50" 
                       style="object-fit: cover;" alt="${item.name}">
                  <span class="fw-medium">${item.name}</span>
                </div>
              </td>
              <td>$${formatPrice(item.price)}</td>
              <td>
                <div class="input-group input-group-sm" style="width: 100px;">
                  <button class="btn btn-outline-secondary" onclick="updateCartQuantity('${item.id}', ${item.quantity - 1})">-</button>
                  <input type="number" class="form-control text-center" value="${item.quantity}" 
                         onchange="updateCartQuantity('${item.id}', parseInt(this.value))" min="1">
                  <button class="btn btn-outline-secondary" onclick="updateCartQuantity('${item.id}', ${item.quantity + 1})">+</button>
                </div>
              </td>
              <td class="fw-bold">$${formatPrice(item.price * item.quantity)}</td>
              <td>
                <button class="btn btn-outline-danger btn-sm" onclick="removeFromCart('${item.id}')">
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
  
  if (elements.cartTotal) {
    elements.cartTotal.textContent = `$${formatPrice(getCartTotal())}`;
  }
}

// Make functions globally accessible
window.updateCartQuantity = updateCartQuantity;
window.removeFromCart = removeFromCart;

function handleClearCart() {
  if (cart.length === 0) {
    showToast('Your cart is already empty', 'info');
    return;
  }
  
  if (confirm('Are you sure you want to clear your cart?')) {
    cart = [];
    saveCart();
    updateCartUI();
    showToast('Cart cleared successfully', 'success');
  }
}

// ============== CHECKOUT ==============
function handleCheckout() {
  if (cart.length === 0) {
    showToast('Your cart is empty', 'warning');
    return;
  }
  
  if (!currentUser) {
    showToast('Please login to checkout', 'warning');
    return;
  }
  
  showCheckoutModal();
}

function showCheckoutModal() {
  bootstrap.Modal.getInstance(document.getElementById('cartModal'))?.hide();
  const checkoutModal = new bootstrap.Modal(document.getElementById('checkoutModal'));
  checkoutModal.show();
}

async function handleCheckoutSubmit(e) {
  e.preventDefault();
  
  if (!currentUser) {
    showToast('Please login to place an order', 'error');
    return;
  }
  
  const submitBtn = e.target.querySelector('button[type="submit"]');
  const originalText = submitBtn.innerHTML;
  submitBtn.disabled = true;
  submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Processing...';
  
  try {
    const token = await getIdToken(currentUser, true);
    
    const orderData = {
      userId: currentUser.uid,
      userEmail: currentUser.email,
      userName: currentUser.displayName,
      items: cart.map(item => ({
        productId: String(item.id),
        productName: item.name,
        productImage: item.image,
        quantity: item.quantity,
        unitPrice: item.price,
        totalPrice: item.price * item.quantity
      })),
      subtotal: getCartTotal(),
      tax: 0,
      shippingCost: 0,
      total: getCartTotal(),
      paymentMethod: 'COD',
      shippingAddress: {
        fullName: document.getElementById('shippingName').value,
        phone: document.getElementById('shippingPhone').value,
        street: document.getElementById('shippingStreet').value,
        city: document.getElementById('shippingCity').value,
        postalCode: document.getElementById('shippingPostal').value,
        country: 'USA'
      },
      notes: document.getElementById('orderNotes').value
    };
    
    const response = await fetch(`${API_BASE_URL}/user/orders`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(orderData)
    });
    
    if (response.ok) {
      const order = await response.json();
      
      // Clear cart
      cart = [];
      saveCart();
      updateCartUI();
      
      // Close modal
      bootstrap.Modal.getInstance(document.getElementById('checkoutModal'))?.hide();
      
      showToast(`Order placed successfully! Order #${order.id?.substring(0, 8) || 'CREATED'}`, 'success');
      
      // Reset form
      e.target.reset();
      
    } else {
      throw new Error('Failed to place order');
    }
    
  } catch (error) {
    console.error('Checkout failed:', error);
    showToast('Checkout failed. Please try again.', 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.innerHTML = originalText;
  }
}

// ============== WISHLIST ==============
function getWishlist() {
  return JSON.parse(localStorage.getItem('bsms_wishlist') || '[]');
}

function isWishlisted(productId) {
  return getWishlist().includes(String(productId));
}

async function toggleWishlist(productId, button) {
  let wishlist = getWishlist();
  const productIdStr = String(productId);
  
  if (wishlist.includes(productIdStr)) {
    wishlist = wishlist.filter(id => id !== productIdStr);
    button.classList.remove('active');
    button.innerHTML = '<i class="bi bi-heart"></i>';
    showToast('Removed from wishlist', 'info');
  } else {
    wishlist.push(productIdStr);
    button.classList.add('active');
    button.innerHTML = '<i class="bi bi-heart-fill"></i>';
    showToast('Added to wishlist!', 'success');
  }
  
  localStorage.setItem('bsms_wishlist', JSON.stringify(wishlist));
  
  // Sync with backend if logged in
  if (currentUser) {
    try {
      const token = await getIdToken(currentUser, true);
      const method = wishlist.includes(productIdStr) ? 'POST' : 'DELETE';
      await fetch(`${API_BASE_URL}/user/wishlist/${currentUser.uid}/${productId}`, {
        method,
        headers: { 'Authorization': `Bearer ${token}` }
      });
    } catch (error) {
      console.warn('Wishlist sync failed:', error);
    }
  }
}

// ============== UTILITIES ==============
function getStockBadgeClass(stock) {
  if (stock > 10) return 'badge-success';
  if (stock > 0) return 'badge-warning';
  return 'badge-danger';
}

function getStockText(stock) {
  if (stock > 10) return 'In Stock';
  if (stock > 0) return 'Low Stock';
  return 'Out of Stock';
}

function formatPrice(price) {
  return Number(price).toFixed(2);
}

function updateProductCount(count) {
  if (elements.productCountBadge) {
    elements.productCountBadge.textContent = `${count} Products`;
  }
}

function showLoading(show) {
  if (elements.loadingState) {
    elements.loadingState.style.display = show ? 'block' : 'none';
  }
  if (elements.productGrid) {
    elements.productGrid.style.display = show ? 'none' : 'grid';
  }
}

function showNoResults(show) {
  if (elements.noResults) {
    elements.noResults.classList.toggle('d-none', !show);
  }
  if (elements.productGrid) {
    elements.productGrid.classList.toggle('d-none', show);
  }
}

function showError(message) {
  showToast(message, 'error');
  showNoResults(true);
}

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

// ============== TOAST NOTIFICATIONS ==============
function showToast(message, type = 'info') {
  let container = document.getElementById('toastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'position-fixed top-0 end-0 p-3';
    container.style.zIndex = '9999';
    document.body.appendChild(container);
  }
  
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
  
  container.appendChild(toast);
  
  const bsToast = new bootstrap.Toast(toast, {
    autohide: true,
    delay: type === 'error' ? 5000 : 3000
  });
  
  bsToast.show();
  
  toast.addEventListener('hidden.bs.toast', () => toast.remove());
}

// ============== EXPORTS ==============
export async function authorizedFetch(url, options = {}) {
  if (currentUser) {
    const token = await getIdToken(currentUser, true);
    options.headers = Object.assign({}, options.headers, { 
      'Authorization': `Bearer ${token}` 
    });
  }
  return fetch(url, options);
}

export { API_BASE_URL, auth, currentUser };
