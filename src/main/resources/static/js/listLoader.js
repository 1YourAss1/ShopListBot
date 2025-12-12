(function () {
    const tg = window.Telegram.WebApp;
    const userId = tg.initDataUnsafe.user.id;
    const initData = tg.initData;

    const shopList = document.getElementById('shop-list');
    const shopListEmptyItem = document.getElementById('shop-list-empty-item');
    const shopListCompleted = document.getElementById('shop-list-completed');
    const shopListCompletedEmptyItem = document.getElementById('shop-list-completed-empty-item');

    const overlay = document.getElementById('overlay');
    const completedPanel = document.getElementById('completedPanel');
    const completedHeader = document.getElementById('completedHeader');

    completedHeader.addEventListener('click', () => {
        const isOpen = completedPanel.classList.contains('open');
        completedPanel.classList.toggle('open');
        overlay.classList.toggle('active', !isOpen);
        document.body.style.overflow = !isOpen ? 'hidden' : '';
    });

    overlay.addEventListener('click', () => {
        completedPanel.classList.remove('open');
        overlay.classList.remove('active');
        document.body.style.overflow = '';
    });


    async function loadShopList() {
        try {
            const response = await fetch(`api/purchases/${userId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `tma ${initData}`
                }
            });
            const purchases = await response.json();

            purchases.forEach((purchase) => {
                const shopItem = document.createElement('li');
                shopItem.classList.add('item');
                shopItem.innerHTML = `
                            <input id="${purchase.id}" type="checkbox" class="checkbox" ${purchase.completed ? 'checked' : ''}>
                            <label for="${purchase.id}" class="item-text">${capitalizeFirstLetter(purchase.product.name) + getQuantityStr(purchase.quantity) + getFioString(purchase.user)}</label>
                        `;
                shopItem.addEventListener('change', async (event) => {
                    const checkbox = event.target;
                    const item = checkbox.closest('li');
                    const checked = checkbox.checked;
                    const checkedBefore = !checkbox.checked;

                    moveItem(item, checked);
                    const response = await fetch(`api/toggle`, {
                        method: 'PATCH',
                        headers: {
                            'Authorization': `tma ${initData}`,
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            id: checkbox.id,
                            completed: checked
                        })
                    });
                    if (!response.ok) {
                        checkbox.checked = checkedBefore;
                        moveItem(item, checkedBefore)
                        console.error('Не удалось обновить статус');
                    }
                });

                if (purchase.completed) {
                    shopListCompleted.appendChild(shopItem);
                } else {
                    shopList.appendChild(shopItem);
                }
            });
            updateEmptyItems();
        } catch (error) {
            console.log(error);
        }
    }

    function capitalizeFirstLetter(val) {
        return String(val).charAt(0).toUpperCase() + String(val).slice(1);
    }

    function getQuantityStr(quantity) {
        if (quantity) {
            return ` (${quantity})`;
        } else {
            return '';
        }
    }

    function getFioString(user) {
        let fioString = ` (${user.id})`;
        if (user.first_name) {
            fioString = ` (${user.first_name})`;
            if (user.last_name) {
                fioString = ` (${user.first_name} ${user.last_name})`;
            }
        } else if (user.username) {
            fioString = ` (${user.username})`;
        }
        return fioString;
    }

    function moveItem(li, completed) {
        li.classList.toggle('checked', completed);
        li.querySelector('.checkbox')?.classList.toggle('checked', completed);

        li.remove();

        if (completed) {
            shopListCompleted.appendChild(li);
        } else {
            shopList.appendChild(li);
        }

        updateEmptyItems();
    }

    function updateEmptyItems() {
        const hasActive = shopList.children.length > 1;
        const hasCompleted = shopListCompleted.children.length > 1;

        shopListEmptyItem.style.display = hasActive ? 'none' : 'flex';
        shopListCompletedEmptyItem.style.display = hasCompleted ? 'none' : 'flex';
    }

    tg.ready();
    loadShopList();
})();