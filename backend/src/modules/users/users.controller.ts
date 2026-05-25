import { Controller, Get, Req, UseGuards } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { JwtPayload } from '../auth/auth.service';

@Controller('users')
export class UsersController {
  constructor(private readonly prisma: PrismaService) {}

  @Get('me')
  @UseGuards(JwtAuthGuard)
  async me(@Req() req: { user: JwtPayload }) {
    const user = await this.prisma.user.findUnique({
      where: { id: req.user.sub },
      include: { settings: true },
    });
    return user;
  }
}
