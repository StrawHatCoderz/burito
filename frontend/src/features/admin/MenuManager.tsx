import { useState, useEffect } from 'react'
import {
  Box,
  Typography,
  Button,
  Grid,
  Card,
  CardMedia,
  CardContent,
  CardActions,
  IconButton,
  Chip,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Snackbar,
  Alert
} from '@mui/material'
import { SvgIcon } from '@mui/material'
import type { SvgIconProps } from '@mui/material'
import { getAdminMenu, addMenuItem, updateMenuItem, deleteMenuItem } from './adminApi'
import type { MenuItemPayload } from './adminApi'
import { extractErrorMessage } from '../../shared/api/types'
import type { MenuItem } from '../catalog/types'
import { MenuItemForm } from './MenuItemForm'

const AddIcon = (props: SvgIconProps) => (
  <SvgIcon {...props}>
    <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" />
  </SvgIcon>
)

const EditIcon = (props: SvgIconProps) => (
  <SvgIcon {...props}>
    <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
  </SvgIcon>
)

const DeleteIcon = (props: SvgIconProps) => (
  <SvgIcon {...props}>
    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z" />
  </SvgIcon>
)

const DEFAULT_ITEM_IMAGE = 'https://placehold.co/400x300/eeeeee/999999?text=No+Image'

interface MenuManagerProps {
  restaurantId: string
}

export function MenuManager({ restaurantId }: MenuManagerProps) {
  const [menuItems, setMenuItems] = useState<MenuItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  
  const [formOpen, setFormOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<MenuItem | null>(null)
  
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false)
  const [itemToDelete, setItemToDelete] = useState<MenuItem | null>(null)
  
  const [successMsg, setSuccessMsg] = useState<string | null>(null)

  const fetchMenu = async () => {
    try {
      setLoading(true)
      const items = await getAdminMenu(restaurantId)
      setMenuItems(items)
      setError(null)
    } catch (err: any) {
      setError(extractErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchMenu()
  }, [restaurantId])

  const handleOpenAdd = () => {
    setEditingItem(null)
    setFormOpen(true)
  }

  const handleOpenEdit = (item: MenuItem) => {
    setEditingItem(item)
    setFormOpen(true)
  }

  const handleFormSubmit = async (payload: MenuItemPayload) => {
    if (editingItem) {
      await updateMenuItem(restaurantId, editingItem.menuItemId, payload)
      setSuccessMsg('Menu item updated successfully')
    } else {
      await addMenuItem(restaurantId, payload)
      setSuccessMsg('Menu item added successfully')
    }
    fetchMenu()
  }

  const handleDeleteClick = (item: MenuItem) => {
    setItemToDelete(item)
    setDeleteConfirmOpen(true)
  }

  const confirmDelete = async () => {
    if (!itemToDelete) return
    try {
      await deleteMenuItem(restaurantId, itemToDelete.menuItemId)
      setSuccessMsg('Menu item deleted successfully')
      fetchMenu()
    } catch (err: any) {
      setError(extractErrorMessage(err))
    } finally {
      setDeleteConfirmOpen(false)
      setItemToDelete(null)
    }
  }

  if (loading && menuItems.length === 0) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress sx={{ color: '#D34A24' }} />
      </Box>
    )
  }

  return (
    <Box sx={{ mt: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight={800} color="text.primary">
          Menu Management
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleOpenAdd}
          sx={{
            borderRadius: '12px',
            fontWeight: 700,
            textTransform: 'none',
            backgroundColor: '#D34A24',
            '&:hover': { backgroundColor: '#B33A1A' }
          }}
        >
          Add Item
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 3, borderRadius: { xs: '12px', md: '8px' } }}>{error}</Alert>}

      {(menuItems || []).length === 0 ? (
        <Box sx={{ textAlign: 'center', py: 6, backgroundColor: 'rgba(0,0,0,0.02)', borderRadius: { xs: '16px', md: '8px' } }}>
          <Typography variant="h6" color="text.secondary" fontWeight={600}>
            Your menu is empty
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Start adding delicious items to your menu to attract customers.
          </Typography>
          <Button variant="outlined" startIcon={<AddIcon />} onClick={handleOpenAdd} sx={{ borderRadius: '8px' }}>
            Add First Item
          </Button>
        </Box>
      ) : (
        <Grid container spacing={3}>
          {(menuItems || []).map((item) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={item.menuItemId}>
              <Card sx={{ 
                height: '100%', 
                display: 'flex', 
                flexDirection: 'column',
                borderRadius: { xs: '16px', md: '8px' },
                boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                border: '1px solid rgba(0,0,0,0.08)',
                transition: 'transform 0.2s, box-shadow 0.2s',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: '0 8px 24px rgba(0,0,0,0.1)',
                }
              }}>
                <Box sx={{ position: 'relative' }}>
                  <CardMedia
                    component="img"
                    height="140"
                    image={item.imageUrl || DEFAULT_ITEM_IMAGE}
                    alt={item.name}
                    sx={{ backgroundColor: '#f5f5f5' }}
                  />
                  {!item.available && (
                    <Box sx={{
                      position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
                      backgroundColor: 'rgba(0,0,0,0.5)',
                      display: 'flex', alignItems: 'center', justifyContent: 'center'
                    }}>
                      <Chip label="Sold Out / Hidden" color="default" sx={{ backgroundColor: 'rgba(255,255,255,0.9)', fontWeight: 700 }} />
                    </Box>
                  )}
                </Box>
                <CardContent sx={{ flexGrow: 1, p: 2.5 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                    <Typography variant="h6" fontWeight={700} sx={{ lineHeight: 1.2 }}>
                      {item.name}
                    </Typography>
                    <Typography variant="h6" color="#D34A24" fontWeight={800}>
                      ₹{item.price.toFixed(2)}
                    </Typography>
                  </Box>
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 1, fontWeight: 600, letterSpacing: '0.5px', textTransform: 'uppercase' }}>
                    {item.category}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{
                    display: '-webkit-box',
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden'
                  }}>
                    {item.description || 'No description provided.'}
                  </Typography>
                </CardContent>
                <CardActions sx={{ px: 2, pb: 2, pt: 0, justifyContent: 'flex-end' }}>
                  <IconButton onClick={() => handleOpenEdit(item)} size="small" sx={{ color: 'text.secondary', '&:hover': { color: 'primary.main', backgroundColor: 'rgba(211,74,36,0.1)' } }}>
                    <EditIcon fontSize="small" />
                  </IconButton>
                  <IconButton onClick={() => handleDeleteClick(item)} size="small" sx={{ color: 'text.secondary', '&:hover': { color: 'error.main', backgroundColor: 'rgba(211,47,47,0.1)' } }}>
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      <MenuItemForm
        open={formOpen}
        onClose={() => setFormOpen(false)}
        initialData={editingItem}
        onSubmit={handleFormSubmit}
      />

      <Dialog open={deleteConfirmOpen} onClose={() => setDeleteConfirmOpen(false)} PaperProps={{ sx: { borderRadius: '16px' } }}>
        <DialogTitle sx={{ fontWeight: 800 }}>Delete Menu Item</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete <b>{itemToDelete?.name}</b>? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions sx={{ p: 2.5 }}>
          <Button onClick={() => setDeleteConfirmOpen(false)} sx={{ fontWeight: 600, color: 'text.secondary' }}>Cancel</Button>
          <Button onClick={confirmDelete} variant="contained" color="error" sx={{ fontWeight: 700, borderRadius: '8px', boxShadow: 'none' }}>
            Delete Permanently
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={!!successMsg} autoHideDuration={4000} onClose={() => setSuccessMsg(null)} anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
        <Alert onClose={() => setSuccessMsg(null)} severity="success" sx={{ width: '100%', borderRadius: '12px' }}>
          {successMsg}
        </Alert>
      </Snackbar>
    </Box>
  )
}
