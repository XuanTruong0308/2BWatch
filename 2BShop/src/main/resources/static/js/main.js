// ===================================
// 2BSHOP - Main JavaScript
// ===================================

document.addEventListener('DOMContentLoaded', function() {
    // Smooth scroll for anchor links
    const links = document.querySelectorAll('a[href^="#"]');
    links.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
    // Add fade-in animation on scroll
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in');
            }
        });
    }, observerOptions);
    
    document.querySelectorAll('.product-card, .section-title').forEach(el => {
        observer.observe(el);
    });
});

// ===================================
// BACKEND INTEGRATION FUNCTIONS
// ===================================

// Add to Cart
// BACKEND: POST /cart/add
// Request: { watchId: number, quantity: number }
// Response: { success: boolean, message: string, cartItemCount: number }
function addToCart(watchId) {
    fetch('/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            watchId: watchId,
            quantity: 1
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            updateCartBadge();
            showNotification('Đã thêm sản phẩm vào giỏ hàng!', 'success');
        } else {
            showNotification(data.message || 'Có lỗi xảy ra!', 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showNotification('Có lỗi xảy ra!', 'error');
    });
}

// Update cart badge count
// BACKEND: GET /cart/count
// Response: { count: number }
function updateCartBadge() {
    fetch('/cart/count')
        .then(response => response.json())
        .then(data => {
            const badge = document.querySelector('.cart-badge');
            if (badge) {
                badge.textContent = data.count;
                if (data.count > 0) {
                    badge.style.display = 'block';
                } else {
                    badge.style.display = 'none';
                }
            }
        })
        .catch(error => console.error('Error updating cart:', error));
}

// Show notification
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 25px;
        background: ${type === 'success' ? '#28a745' : '#dc3545'};
        color: white;
        border-radius: 4px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 10000;
        animation: slideIn 0.3s ease-out;
    `;
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// CSS Animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(400px); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(400px); opacity: 0; }
    }
`;
document.head.appendChild(style);

// ===================================
// CART PAGE FUNCTIONS
// ===================================

// Update cart item quantity
// BACKEND: POST /cart/update
// Request: { cartItemId: number, quantity: number }
function updateCartQuantity(cartItemId, change) {
    const quantityInput = event.target.closest('.item-quantity').querySelector('input');
    const currentQty = parseInt(quantityInput.value);
    const newQty = currentQty + change;
    
    if (newQty < 1) return;
    
    fetch('/cart/update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            cartItemId: cartItemId,
            quantity: newQty
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Reload page to update totals
            location.reload();
        } else {
            showNotification(data.message || 'Không thể cập nhật số lượng!', 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showNotification('Có lỗi xảy ra!', 'error');
    });
}

// Remove cart item
// BACKEND: POST /cart/remove
// Request: { cartItemId: number }
function removeCartItem(cartItemId) {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;
    
    fetch('/cart/remove', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            cartItemId: cartItemId
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showNotification('Đã xóa sản phẩm khỏi giỏ hàng!', 'success');
            setTimeout(() => location.reload(), 1000);
        } else {
            showNotification(data.message || 'Không thể xóa sản phẩm!', 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showNotification('Có lỗi xảy ra!', 'error');
    });
}

// ===================================
// SMOOTH PAGE TRANSITIONS
// ===================================

// Add page transition effect
document.addEventListener('DOMContentLoaded', function() {
    // Add fade-in to page content
    document.body.style.opacity = '0';
    document.body.style.transition = 'opacity 0.3s ease-in';
    
    setTimeout(() => {
        document.body.style.opacity = '1';
    }, 100);
    
    // Smooth transitions on link clicks
    const internalLinks = document.querySelectorAll('a:not([href^="#"]):not([target="_blank"])');
    internalLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            if (href && href !== '' && !href.startsWith('#')) {
                e.preventDefault();
                document.body.style.opacity = '0';
                setTimeout(() => {
                    window.location.href = href;
                }, 300);
            }
        });
    });
});

// ===================================
// PRODUCT DETAIL PAGE
// ===================================

function changeMainImage(imageUrl) {
    const mainImg = document.getElementById('mainProductImage');
    if (mainImg) {
        mainImg.style.opacity = '0';
        setTimeout(() => {
            mainImg.src = imageUrl;
            mainImg.style.opacity = '1';
        }, 200);
    }
    
    // Update active thumbnail
    document.querySelectorAll('.thumbnail').forEach(thumb => {
        thumb.classList.remove('active');
    });
    if (event) {
        event.currentTarget.classList.add('active');
    }
}

function increaseQuantity() {
    const input = document.getElementById('quantity');
    if (input) {
        const max = parseInt(input.max) || 999;
        const current = parseInt(input.value) || 1;
        if (current < max) {
            input.value = current + 1;
        }
    }
}

function decreaseQuantity() {
    const input = document.getElementById('quantity');
    if (input) {
        const current = parseInt(input.value) || 1;
        if (current > 1) {
            input.value = current - 1;
        }
    }
}

// Add to cart from product detail page
function addToCartFromDetail(watchId) {
    const quantityInput = document.getElementById('quantity');
    const quantity = quantityInput ? parseInt(quantityInput.value) : 1;
    
    fetch('/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            watchId: watchId,
            quantity: quantity
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            updateCartBadge();
            showNotification(`Đã thêm ${quantity} sản phẩm vào giỏ hàng!`, 'success');
            
            // Add animation to cart icon
            const cartIcon = document.querySelector('.nav-icon .fa-shopping-cart');
            if (cartIcon) {
                cartIcon.classList.add('bounce');
                setTimeout(() => cartIcon.classList.remove('bounce'), 500);
            }
        } else {
            showNotification(data.message || 'Có lỗi xảy ra!', 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showNotification('Có lỗi xảy ra!', 'error');
    });
}

// ===================================
// FORM VALIDATION
// ===================================

// Checkout form validation
document.addEventListener('DOMContentLoaded', function() {
    const checkoutForm = document.getElementById('checkoutForm');
    if (checkoutForm) {
        checkoutForm.addEventListener('submit', function(e) {
            const phone = this.querySelector('input[name="phone"]').value;
            const phoneRegex = /^[0-9]{10}$/;
            
            if (!phoneRegex.test(phone.replace(/\s/g, ''))) {
                e.preventDefault();
                showNotification('Số điện thoại không hợp lệ! Vui lòng nhập 10 số.', 'error');
                return false;
            }
            
            // Show loading
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
            }
        });
    }
});

// ===================================
// SEARCH FUNCTIONALITY
// ===================================

// Auto-submit search after delay
let searchTimeout;
document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.querySelector('.search-bar input');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                if (this.value.length >= 2) {
                    // Auto-search
                    this.closest('form').submit();
                }
            }, 800);
        });
    }
});

// ===================================
// IMAGE LAZY LOADING
// ===================================

document.addEventListener('DOMContentLoaded', function() {
    const images = document.querySelectorAll('img[data-src]');
    
    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.removeAttribute('data-src');
                observer.unobserve(img);
            }
        });
    });
    
    images.forEach(img => imageObserver.observe(img));
});

// ===================================
// UTILITY FUNCTIONS
// ===================================

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Scroll to top button
window.addEventListener('scroll', function() {
    const scrollBtn = document.getElementById('scrollToTop');
    if (scrollBtn) {
        if (window.pageYOffset > 300) {
            scrollBtn.style.display = 'flex';
        } else {
            scrollBtn.style.display = 'none';
        }
    }
});

function scrollToTop() {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
}

// Add bounce animation CSS
const bounceStyle = document.createElement('style');
bounceStyle.textContent = `
    @keyframes bounce {
        0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
        40% { transform: translateY(-10px); }
        60% { transform: translateY(-5px); }
    }
    .bounce {
        animation: bounce 0.5s;
    }
`;
document.head.appendChild(bounceStyle);

// Console message
console.log('%c2BSHOP ', 'font-size: 20px; font-weight: bold; color: #000;');
console.log('%cWelcome to 2BSHOP - Premium Watch Store', 'font-size: 14px; color: #666;');
